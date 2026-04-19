import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const isSeller = user?.roles?.includes('ROLE_SELLER');

  return (
    <nav className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          <div className="flex items-center">
            <Link to="/" className="text-2xl font-bold text-primary">
              E-Market
            </Link>
          </div>
          
          <div className="flex items-center gap-6">
            <Link to="/" className="text-gray-600 hover:text-primary transition">Trang chủ</Link>
            
            {user ? (
              <>
                {isSeller ? (
                  <Link to="/my-shop" className="text-gray-600 hover:text-primary font-medium">Kênh Người Bán</Link>
                ) : (
                  <Link to="/register-shop" className="text-gray-600 hover:text-primary font-medium">Trở thành Người bán</Link>
                )}
                
                <div className="flex items-center gap-2 border-l pl-6">
                  <span className="text-sm font-medium text-gray-700">{user.username}</span>
                  <button 
                    onClick={() => { logout(); navigate('/login'); }}
                    className="text-sm text-red-500 hover:underline"
                  >
                    Đăng xuất
                  </button>
                </div>
              </>
            ) : (
              <div className="flex gap-4">
                <Link to="/login" className="text-gray-600 hover:text-primary">Đăng nhập</Link>
                <Link to="/register" className="bg-primary text-white px-4 py-2 rounded-md hover:bg-red-600">Đăng ký</Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
