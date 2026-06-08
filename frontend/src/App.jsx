import React from 'react';
import { BrowserRouter as Router, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import ProductDetail from './pages/ProductDetail';
import Login from './pages/Login';
import Register from './pages/Register';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import MyOrders from './pages/MyOrders';
import Profile from './pages/Profile';
import Wishlist from './pages/Wishlist';
import AdminDashboard from './pages/AdminDashboard';
import AdminProducts from './pages/AdminProducts';
import ProductForm from './pages/ProductForm';
import AdminBrands from './pages/AdminBrands';
import AdminCategories from './pages/AdminCategories';
import AdminOrders from './pages/AdminOrders';
import AdminUsers from './pages/AdminUsers';
import VnpayReturn from './pages/VnpayReturn';

const PageShell = ({ children }) => (
  <>
    <Navbar />
    {children}
  </>
);

const ProtectedRoute = ({ children, role }) => {
  const { user } = useAuth();

  if (!user) return <Navigate to="/login" replace />;
  if (role && !user.roles?.includes(role)) return <Navigate to="/" replace />;

  return <PageShell>{children}</PageShell>;
};

const PublicStoreRoute = ({ children }) => {
  const { user } = useAuth();

  if (user?.roles?.includes('ROLE_ADMIN')) {
    return <Navigate to="/admin" replace />;
  }

  return <PageShell>{children}</PageShell>;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-bg-soft">
          <Routes>
            <Route path="/" element={<PublicStoreRoute><Home /></PublicStoreRoute>} />
            <Route path="/products/:id" element={<PublicStoreRoute><ProductDetail /></PublicStoreRoute>} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route path="/cart" element={<ProtectedRoute><Cart /></ProtectedRoute>} />
            <Route path="/checkout" element={<ProtectedRoute><Checkout /></ProtectedRoute>} />
            <Route path="/payment/vnpay-return" element={<ProtectedRoute><VnpayReturn /></ProtectedRoute>} />
            <Route path="/wishlist" element={<ProtectedRoute><Wishlist /></ProtectedRoute>} />
            <Route path="/my-orders" element={<ProtectedRoute><MyOrders /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />

            <Route path="/admin" element={<ProtectedRoute role="ROLE_ADMIN"><AdminDashboard /></ProtectedRoute>} />
            <Route path="/admin/products" element={<ProtectedRoute role="ROLE_ADMIN"><AdminProducts /></ProtectedRoute>} />
            <Route path="/admin/products/new" element={<ProtectedRoute role="ROLE_ADMIN"><ProductForm /></ProtectedRoute>} />
            <Route path="/admin/products/:id/edit" element={<ProtectedRoute role="ROLE_ADMIN"><ProductForm /></ProtectedRoute>} />
            <Route path="/admin/brands" element={<ProtectedRoute role="ROLE_ADMIN"><AdminBrands /></ProtectedRoute>} />
            <Route path="/admin/categories" element={<ProtectedRoute role="ROLE_ADMIN"><AdminCategories /></ProtectedRoute>} />
            <Route path="/admin/orders" element={<ProtectedRoute role="ROLE_ADMIN"><AdminOrders /></ProtectedRoute>} />
            <Route path="/admin/users" element={<ProtectedRoute role="ROLE_ADMIN"><AdminUsers /></ProtectedRoute>} />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
