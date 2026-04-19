import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import RegisterShop from './pages/RegisterShop';
import MyShop from './pages/MyShop';

const ProtectedRoute = ({ children, role }) => {
  if (!user) return <Navigate to="/login" />;
  
  if (role && !user.roles?.includes(role)) {
    return <Navigate to="/" />;
  }
  
  return (
    <>
      <Navbar />
      {children}
    </>
  );
};

const Home = () => {
  const { user } = useAuth();
  return (
    <div className="p-8 max-w-7xl mx-auto">
      <div className="bg-white rounded-lg shadow-sm p-8 border">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">Chào mừng, {user?.username}!</h1>
        <p className="text-xl text-gray-600 mb-8">
          Bạn đang ở trong hệ thống Marketplace thế hệ mới.
        </p>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="bg-orange-50 p-6 rounded-xl border border-orange-100">
            <h3 className="text-lg font-bold text-orange-800 mb-2">🛍️ Mua sắm ngay</h3>
            <p className="text-orange-700">Khám phá hàng ngàn sản phẩm từ các shop uy tín.</p>
            <button className="mt-4 bg-primary text-white px-6 py-2 rounded-lg font-bold">Xem Sản phẩm</button>
          </div>
          
          {!user?.roles?.includes('ROLE_SELLER') && (
            <div className="bg-blue-50 p-6 rounded-xl border border-blue-100">
              <h3 className="text-lg font-bold text-blue-800 mb-2">🚀 Kinh doanh cùng chúng tôi</h3>
              <p className="text-blue-700">Mở gian hàng hoàn toàn miễn phí và bắt đầu bán hàng.</p>
              <button 
                onClick={() => window.location.href='/register-shop'}
                className="mt-4 bg-blue-600 text-white px-6 py-2 rounded-lg font-bold"
              >
                Mở Shop Ngay
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-gray-50">
          <Routes>
            <Route path="/" element={
              <ProtectedRoute>
                <Home />
              </ProtectedRoute>
            } />
            <Route path="/register-shop" element={
              <ProtectedRoute>
                <RegisterShop />
              </ProtectedRoute>
            } />
            <Route path="/my-shop" element={
              <ProtectedRoute role="ROLE_SELLER">
                <MyShop />
              </ProtectedRoute>
            } />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
