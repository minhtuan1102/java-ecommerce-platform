import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const isSeller = user?.roles?.includes('ROLE_SELLER');
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <nav className="sticky top-0 z-50 bg-white/80 backdrop-blur-xl border-b border-gray-100 shadow-[0_4px_30px_rgba(0,0,0,0.03)]">
      <div className="max-w-7xl mx-auto px-6">
        <div className="flex justify-between h-20 items-center">
          <div className="flex items-center">
            <Link to="/" className="group flex items-center gap-2">
              <div className="w-10 h-10 bg-primary rounded-xl rotate-12 group-hover:rotate-0 transition-all duration-300 flex items-center justify-center shadow-lg shadow-primary/30">
                <span className="text-white font-black text-xl -rotate-12 group-hover:rotate-0 transition-all duration-300">E</span>
              </div>
              <span className="text-xl font-black text-dark tracking-tight">EMARKET.</span>
            </Link>
          </div>
          
          <div className="hidden md:flex items-center gap-10">
            <Link to="/" className="text-sm font-black text-gray-400 hover:text-dark transition relative after:absolute after:bottom-[-4px] after:left-0 after:w-0 after:h-[2px] after:bg-primary hover:after:w-full after:transition-all tracking-widest uppercase">KHÁM PHÁ</Link>
            
            {user ? (
              <>
                <Link to="/cart" className="relative group flex items-center gap-2">
                  <div className="w-12 h-12 rounded-full bg-gray-50 flex items-center justify-center group-hover:bg-primary/5 transition-colors">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-gray-500 group-hover:text-primary transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                    </svg>
                  </div>
                  <span className="text-xs font-black text-dark group-hover:text-primary transition-colors tracking-widest uppercase">GIỎ HÀNG</span>
                </Link>
                
                <div className="h-6 w-px bg-gray-100 mx-2"></div>

                <div className="flex items-center gap-8">
                  {isAdmin && (
                    <div className="flex gap-6 border-r pr-6">
                      <Link to="/admin/users" className="text-[10px] font-black text-gray-400 hover:text-dark uppercase tracking-widest">QUẢN LÝ USER</Link>
                      <Link to="/admin/products" className="text-[10px] font-black text-primary hover:text-primary-dark uppercase tracking-widest">DUYỆT SP</Link>
                    </div>
                  )}
                  
                  {isSeller ? (
                    <Link to="/my-shop" className="text-xs font-black bg-accent text-white px-4 py-2 rounded-xl shadow-sm shadow-accent/20 hover:scale-105 transition-transform uppercase tracking-wider">KÊNH NGƯỜI BÁN</Link>
                  ) : (
                    <Link to="/register-shop" className="text-[10px] font-black text-primary border-b-2 border-primary pb-0.5 hover:text-primary-dark hover:border-primary-dark transition-all uppercase tracking-widest">TRỞ THÀNH NGƯỜI BÁN</Link>
                  )}
                  
                  <Link to="/profile" className="flex items-center gap-3 pl-2 group">
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-primary to-indigo-400 p-[2px]">
                      <div className="w-full h-full rounded-[10px] bg-white flex items-center justify-center overflow-hidden">
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-primary" viewBox="0 0 20 20" fill="currentColor">
                          <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                        </svg>
                      </div>
                    </div>
                    <span className="text-sm font-black text-dark group-hover:text-primary transition-colors uppercase tracking-widest">{user.username}</span>
                  </Link>
                  
                  <button onClick={handleLogout} className="text-xs font-black text-gray-300 hover:text-red-500 uppercase tracking-widest">THOÁT</button>
                </div>
              </>
            ) : (
              <div className="flex gap-6 items-center">
                <Link to="/login" className="text-sm font-black text-gray-500 hover:text-dark transition tracking-widest uppercase">ĐĂNG NHẬP</Link>
                <Link to="/register" className="bg-dark text-white px-10 py-4 rounded-2xl font-black hover:bg-primary transition shadow-xl shadow-dark/10 active:scale-95 text-xs tracking-[0.2em] uppercase">BẮT ĐẦU</Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
