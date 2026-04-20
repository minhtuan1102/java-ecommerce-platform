import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const response = await api.post('/auth/login', formData);
      const { id, username, email, roles, accessToken, refreshToken } = response.data;
      login({ id, username, email, roles }, accessToken, refreshToken);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen bg-white">
      {/* Trái: Trang trí */}
      <div className="hidden lg:flex lg:w-1/2 bg-dark items-center justify-center p-12">
        <div className="max-w-md">
          <h1 className="text-7xl font-black text-white tracking-tighter leading-none mb-8 uppercase">
            GIA NHẬP <br /> <span className="text-primary italic">CÂU LẠC BỘ.</span>
          </h1>
          <p className="text-gray-400 font-bold uppercase tracking-[0.2em] text-[10px]">
            Quyền truy cập độc quyền vào bộ sưu tập tuyển chọn
          </p>
        </div>
      </div>

      {/* Phải: Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 md:p-16">
        <div className="w-full max-w-sm">
          <div className="mb-12">
            <Link to="/" className="text-2xl font-black text-dark tracking-tighter">EMARKET.</Link>
            <h2 className="text-3xl font-black text-dark mt-8 uppercase tracking-tight">Chào mừng trở lại</h2>
            <p className="text-gray-400 text-sm font-medium mt-2">Nhập thông tin của bạn để truy cập tài khoản</p>
          </div>

          {error && (
            <div className="bg-red-50 text-red-600 p-4 rounded-xl mb-8 text-[10px] font-black uppercase tracking-widest border-l-4 border-red-600">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-8">
            <div className="space-y-6">
              <div className="group">
                <label className="block text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-2 group-focus-within:text-primary transition-colors">Địa chỉ Email</label>
                <input
                  type="email"
                  name="email"
                  placeholder="ten@congty.com"
                  className="w-full pb-3 bg-transparent border-b-2 border-gray-100 focus:border-dark outline-none transition-all font-bold text-dark placeholder:text-gray-200"
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="group">
                <label className="block text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-2 group-focus-within:text-primary transition-colors">Mật mã bảo mật</label>
                <input
                  type="password"
                  name="password"
                  placeholder="••••••••"
                  className="w-full pb-3 bg-transparent border-b-2 border-gray-100 focus:border-dark outline-none transition-all font-bold text-dark placeholder:text-gray-200"
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`w-full bg-dark text-white py-5 rounded-2xl font-black uppercase tracking-widest transition-all hover:bg-primary active:scale-[0.98] shadow-2xl shadow-dark/10 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
            >
              {loading ? 'Đang xác thực...' : 'Đăng nhập'}
            </button>
          </form>

          <div className="mt-12 pt-8 border-t border-gray-50 text-center">
            <p className="text-gray-400 text-sm font-medium">
              Chưa có tài khoản? <Link to="/register" className="text-dark font-black hover:text-primary transition-colors ml-1 uppercase tracking-tighter">Đăng ký ngay</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
