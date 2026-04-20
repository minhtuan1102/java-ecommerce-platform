import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

const RegisterShop = () => {
  const [formData, setFormData] = useState({ name: '', description: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await api.post('/shops', formData);
      
      // Sau khi tạo shop thành công, role của user trong DB đã là ROLE_SELLER.
      // Chúng ta cập nhật lại state của user ở Frontend để Navbar thay đổi nút.
      const updatedUser = { ...user, roles: Array.from(new Set([...(user.roles || []), 'ROLE_SELLER'])) };
      updateUser(updatedUser);
      
      alert('Đăng ký gian hàng thành công! Bạn đã trở thành Người bán.');
      navigate('/my-shop');
    } catch (err) {
      setError(err.response?.data?.message || 'Không thể tạo shop. Tên shop có thể đã tồn tại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto mt-10 p-8 bg-white rounded-lg shadow">
      <h1 className="text-2xl font-bold mb-6">Đăng ký mở gian hàng</h1>
      <p className="text-gray-600 mb-8">Hãy điền thông tin để bắt đầu bán hàng trên E-Market ngay hôm nay.</p>
      
      {error && <div className="bg-red-50 text-red-600 p-4 rounded mb-6 border border-red-100">{error}</div>}
      
      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Tên gian hàng</label>
          <input
            type="text"
            name="name"
            placeholder="Ví dụ: Tuan's Fashion"
            className="w-full p-3 border border-gray-300 rounded focus:ring-primary focus:border-primary outline-none"
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả gian hàng</label>
          <textarea
            name="description"
            rows="4"
            placeholder="Kể về gian hàng của bạn..."
            className="w-full p-3 border border-gray-300 rounded focus:ring-primary focus:border-primary outline-none"
            onChange={handleChange}
          ></textarea>
        </div>
        <button
          type="submit"
          disabled={loading}
          className={`w-full bg-primary text-white p-3 rounded font-bold uppercase transition ${loading ? 'opacity-70 cursor-not-allowed' : 'hover:bg-red-600'}`}
        >
          {loading ? 'Đang xử lý...' : 'Xác nhận mở gian hàng'}
        </button>
      </form>
    </div>
  );
};

export default RegisterShop;
