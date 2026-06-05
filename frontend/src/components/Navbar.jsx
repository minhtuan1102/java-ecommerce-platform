import React, { useEffect, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

const icons = {
  grid: 'M4 5a1 1 0 0 1 1-1h5v6H4V5Zm10-1h5a1 1 0 0 1 1 1v5h-6V4ZM4 14h6v6H5a1 1 0 0 1-1-1v-5Zm10 0h6v5a1 1 0 0 1-1 1h-5v-6Z',
  storefront: 'M4 8h16l-1.2-4.2A2 2 0 0 0 16.9 2H7.1a2 2 0 0 0-1.9 1.8L4 8Zm0 0v2a3 3 0 0 0 5.5 1.7A3 3 0 0 0 12 13a3 3 0 0 0 2.5-1.3A3 3 0 0 0 20 10V8M6 13v7h12v-7M9 20v-5h6v5',
  cart: 'M3 4h2l2.2 10.3a2 2 0 0 0 2 1.7h7.6a2 2 0 0 0 1.9-1.4L21 8H6M10 20h.01M18 20h.01',
  heart: 'M20.8 4.6a5.4 5.4 0 0 0-7.6 0L12 5.8l-1.2-1.2a5.4 5.4 0 1 0-7.6 7.6L12 21l8.8-8.8a5.4 5.4 0 0 0 0-7.6Z',
  receipt: 'M6 3h12v18l-2-1.2-2 1.2-2-1.2-2 1.2-2-1.2L6 21V3Zm3 5h6M9 12h6M9 16h4',
  user: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm7 9a7 7 0 0 0-14 0',
  package: 'M21 8.5 12 3 3 8.5l9 5.5 9-5.5ZM3 8.5V16l9 5 9-5V8.5M12 14v7',
  category: 'M4 5a2 2 0 0 1 2-2h3v7H4V5Zm11-2h3a2 2 0 0 1 2 2v3h-7V3ZM4 14h7v7H6a2 2 0 0 1-2-2v-5Zm9 0h7v5a2 2 0 0 1-2 2h-5v-7Z',
  tag: 'M20 10 12 2H5a2 2 0 0 0-2 2v7l8 8a3 3 0 0 0 4.2 0l4.8-4.8a3 3 0 0 0 0-4.2ZM8 7h.01',
  users: 'M16 20a6 6 0 0 0-12 0M10 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm10 9a5 5 0 0 0-4-4.9m1-10a3 3 0 0 1 0 6',
  bell: 'M18 8a6 6 0 1 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M13.7 21a2 2 0 0 1-3.4 0',
  logout: 'M10 17 15 12l-5-5M15 12H3M21 3v18h-8',
  menu: 'M4 6h16M4 12h16M4 18h16',
};

const SvgIcon = ({ name, className = 'h-5 w-5' }) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" className={className} fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round">
    <path d={icons[name]} />
  </svg>
);

const navClass = ({ isActive }) =>
  `group inline-flex h-10 items-center gap-2 rounded-lg px-3 text-sm font-semibold transition-colors ${
    isActive ? 'bg-primary text-white shadow-sm shadow-blue-200' : 'text-gray-600 hover:bg-gray-100 hover:text-gray-950'
  }`;

const storeLinks = [
  { to: '/', label: 'Khám phá', icon: 'storefront' },
  { to: '/cart', label: 'Giỏ hàng', icon: 'cart', auth: true },
  { to: '/wishlist', label: 'Đã lưu', icon: 'heart', auth: true },
  { to: '/my-orders', label: 'Đơn mua', icon: 'receipt', auth: true },
];

const adminLinks = [
  { to: '/admin', label: 'Tổng quan', icon: 'grid', end: true },
  { to: '/admin/products', label: 'Sản phẩm', icon: 'package' },
  { to: '/admin/categories', label: 'Danh mục', icon: 'category' },
  { to: '/admin/brands', label: 'Thương hiệu', icon: 'tag' },
  { to: '/admin/orders', label: 'Đơn hàng', icon: 'receipt' },
  { to: '/admin/users', label: 'Người dùng', icon: 'users' },
];

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [cartCount, setCartCount] = useState(0);
  const [pendingOrders, setPendingOrders] = useState(0);
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');
  const links = isAdmin ? adminLinks : storeLinks.filter((link) => !link.auth || user);

  useEffect(() => {
    let active = true;
    if (!user) {
      window.queueMicrotask(() => {
        if (active) {
          setCartCount(0);
          setPendingOrders(0);
        }
      });
      return () => {
        active = false;
      };
    }

    if (isAdmin) {
      api.get('/admin/dashboard/summary')
        .then((response) => {
          if (active) setPendingOrders(Number(response.data?.ordersByStatus?.PENDING || 0));
        })
        .catch(() => {
          if (active) setPendingOrders(0);
        });
    } else {
      api.get('/cart')
        .then((response) => {
          const count = (response.data?.items || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0);
          if (active) setCartCount(count);
        })
        .catch(() => {
          if (active) setCartCount(0);
        });
    }

    return () => {
      active = false;
    };
  }, [user, isAdmin]);

  useEffect(() => {
    const handleCartUpdated = (event) => {
      const count = (event.detail?.items || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0);
      setCartCount(count);
    };

    window.addEventListener('cart:updated', handleCartUpdated);
    return () => window.removeEventListener('cart:updated', handleCartUpdated);
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-50 border-b border-gray-200/80 bg-white/90 shadow-sm backdrop-blur">
      <div className="mx-auto flex min-h-16 max-w-7xl items-center justify-between gap-4 px-4 py-2 md:px-6">
        <Link to={isAdmin ? '/admin' : '/'} className="flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-gray-950 text-white shadow-sm shadow-blue-200">
            <SvgIcon name={isAdmin ? 'grid' : 'storefront'} className="h-6 w-6" />
          </div>
          <div>
            <div className="text-base font-bold text-gray-950">{isAdmin ? 'Bảng quản trị' : 'EMarket'}</div>
            <div className="text-xs font-medium text-gray-500">{isAdmin ? 'Quản lý vận hành' : ''}</div>
          </div>
        </Link>

        <nav className="hidden flex-1 items-center justify-center gap-1 md:flex">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} end={link.end} className={navClass}>
              <span className="relative">
                <SvgIcon name={link.icon} />
                {link.to === '/cart' && cartCount > 0 && (
                  <span className="absolute -right-2 -top-2 min-w-4 rounded-full bg-red-600 px-1 text-center text-[10px] leading-4 text-white">
                    {cartCount > 99 ? '99+' : cartCount}
                  </span>
                )}
                {link.to === '/admin/orders' && pendingOrders > 0 && (
                  <span className="absolute -right-2 -top-2 min-w-4 rounded-full bg-red-600 px-1 text-center text-[10px] leading-4 text-white">
                    {pendingOrders > 99 ? '99+' : pendingOrders}
                  </span>
                )}
              </span>
              <span>{link.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="hidden items-center gap-3 md:flex">
          {user ? (
            <>
              <button type="button" className="relative inline-flex h-10 w-10 items-center justify-center rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50" aria-label="Thông báo">
                <SvgIcon name="bell" />
                {(isAdmin ? pendingOrders : 0) > 0 && <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-red-600" />}
              </button>
              <Link
                to={isAdmin ? '/admin' : '/profile'}
                className="inline-flex h-10 items-center gap-2 rounded-lg border border-gray-200 px-2 text-gray-700 hover:bg-gray-50"
                aria-label="Tài khoản"
              >
                {user.avatarUrl ? (
                  <img src={user.avatarUrl} alt={user.username || user.email || 'Tài khoản'} className="h-8 w-8 rounded-full object-cover" />
                ) : (
                  <span className="flex h-8 w-8 items-center justify-center rounded-full bg-gray-100 text-sm font-bold text-gray-600">
                    {(user.username || user.email || 'U').slice(0, 1).toUpperCase()}
                  </span>
                )}
                <span className="max-w-40 truncate text-sm font-semibold">{user.username || user.email}</span>
              </Link>
              <button onClick={handleLogout} className="inline-flex h-10 items-center gap-2 rounded-lg border border-gray-300 px-3 text-sm font-semibold text-gray-700 hover:bg-gray-50">
                <SvgIcon name="logout" className="h-4 w-4" />
                <span>Đăng xuất</span>
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="inline-flex h-10 items-center rounded-lg border border-gray-300 px-3 text-sm font-semibold text-gray-700 hover:bg-gray-50">
                Đăng nhập
              </Link>
              <Link to="/register" className="inline-flex h-10 items-center rounded-lg bg-primary px-3 text-sm font-semibold text-white hover:bg-primary-dark">
                Đăng ký
              </Link>
            </>
          )}
        </div>

        <button onClick={() => setOpen((value) => !value)} className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-gray-300 text-gray-700 md:hidden" aria-label="Mở menu">
          <SvgIcon name="menu" />
        </button>
      </div>

      {open && (
        <div className="border-t border-gray-200 bg-white px-4 py-3 shadow-sm md:hidden">
          <div className="flex flex-col gap-3">
            {links.map((link) => (
              <NavLink key={link.to} to={link.to} end={link.end} className={navClass} onClick={() => setOpen(false)}>
                <span className="relative">
                  <SvgIcon name={link.icon} />
                  {link.to === '/cart' && cartCount > 0 && (
                    <span className="absolute -right-2 -top-2 min-w-4 rounded-full bg-red-600 px-1 text-center text-[10px] leading-4 text-white">
                      {cartCount > 99 ? '99+' : cartCount}
                    </span>
                  )}
                  {link.to === '/admin/orders' && pendingOrders > 0 && (
                    <span className="absolute -right-2 -top-2 min-w-4 rounded-full bg-red-600 px-1 text-center text-[10px] leading-4 text-white">
                      {pendingOrders > 99 ? '99+' : pendingOrders}
                    </span>
                  )}
                </span>
                <span>{link.label}</span>
              </NavLink>
            ))}
            {user ? (
              <>
                <Link
                  to={isAdmin ? '/admin' : '/profile'}
                  onClick={() => setOpen(false)}
                  className="inline-flex items-center gap-2 rounded-lg border border-gray-200 px-3 py-2 text-sm font-semibold text-gray-700"
                >
                  <SvgIcon name="user" className="h-4 w-4" />
                  <span className="truncate">{user.username || user.email}</span>
                </Link>
                <button onClick={handleLogout} className="inline-flex items-center gap-2 text-left text-sm font-semibold text-red-600">
                  <SvgIcon name="logout" className="h-4 w-4" />
                  <span>Đăng xuất</span>
                </button>
              </>
            ) : (
              <div className="flex gap-2">
                <Link to="/login" className="text-sm font-semibold text-gray-700">Đăng nhập</Link>
                <Link to="/register" className="text-sm font-semibold text-primary">Đăng ký</Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

export default Navbar;
