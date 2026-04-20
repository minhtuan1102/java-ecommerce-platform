import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import RegisterShop from './pages/RegisterShop';
import MyShop from './pages/MyShop';
import AddProduct from './pages/AddProduct';
import Home from './pages/Home';
import Cart from './pages/Cart';
import MyOrders from './pages/MyOrders';
import ShopOrders from './pages/ShopOrders';

const ProtectedRoute = ({ children, role }) => {
  const { user } = useAuth();
  
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
            <Route path="/cart" element={
              <ProtectedRoute>
                <Cart />
              </ProtectedRoute>
            } />
            <Route path="/my-orders" element={
              <ProtectedRoute>
                <MyOrders />
              </ProtectedRoute>
            } />
            <Route path="/my-shop/orders" element={
              <ProtectedRoute role="ROLE_SELLER">
                <ShopOrders />
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
            <Route path="/my-shop/add-product" element={
              <ProtectedRoute role="ROLE_SELLER">
                <AddProduct />
              </ProtectedRoute>
            } />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            {/* Catch-all route */}
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
