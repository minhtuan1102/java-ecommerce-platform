import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';

const Register = () => {
  const [formData, setFormData] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await api.post('/auth/register', formData);
      alert('Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.');
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.');
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
            TẠO <br /> <span className="text-primary italic">TÀI KHOẢN.</span>
          </h1>
          <p className="text-gray-400 font-bold uppercase tracking-[0.2em] text-[10px]">
            Bắt đầu hành trình với sự tinh tuyển tuyệt đối
          </p>
        </div>
      </div>

      {/* Phải: Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 md:p-16">
        <div className="w-full max-w-sm">
          <div className="mb-12">
            <Link to="/" className="text-2xl font-black text-dark tracking-tighter">EMARKET.</Link>
            <h2 className="text-3xl font-black text-dark mt-8 uppercase tracking-tight">Gia nhập cộng đồng</h2>
            <p className="text-gray-400 text-sm font-medium mt-2">Trở thành thành viên của câu lạc bộ mua sắm độc quyền</p>
          </div>

          {error && (
            <div className="bg-red-50 text-red-600 p-4 rounded-xl mb-8 text-[10px] font-black uppercase tracking-widest border-l-4 border-red-600">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-8">
            <div className="space-y-6">
              <div className="group">
                <label className="block text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-2 group-focus-within:text-primary transition-colors">Tên người dùng</label>
                <input
                  type="text"
                  name="username"
                  placeholder="biet_danh_cua_ban"
                  className="w-full pb-3 bg-transparent border-b-2 border-gray-100 focus:border-dark outline-none transition-all font-bold text-dark placeholder:text-gray-200"
                  onChange={handleChange}
                  required
                />
              </div>
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
              {loading ? 'Đang xử lý...' : 'Tạo tài khoản'}
            </button>
          </form>

          <div className="mt-12 pt-8 border-t border-gray-50 text-center">
            <p className="text-gray-400 text-sm font-medium">
              Đã là thành viên? <Link to="/login" className="text-dark font-black hover:text-primary transition-colors ml-1 uppercase tracking-tighter">Đăng nhập</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
