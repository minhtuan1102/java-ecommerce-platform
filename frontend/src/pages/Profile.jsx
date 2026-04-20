import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

const Profile = () => {
  const { updateUser, logout } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({ username: '', email: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const response = await api.get('/auth/me');
        setFormData({ username: response.data.username, email: response.data.email });
        updateUser({
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
          roles: response.data.roles
        });
      } catch (err) {
        setError(err.response?.data?.message || 'Khong the tai thong tin tai khoan.');
      } finally {
        setLoading(false);
      }
    })();
  }, [updateUser]);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');

    try {
      const response = await api.put('/auth/me', formData);
      updateUser({
        id: response.data.id,
        username: response.data.username,
        email: response.data.email,
        roles: response.data.roles
      });
      alert('Cap nhat tai khoan thanh cong.');
    } catch (err) {
      setError(err.response?.data?.message || 'Cap nhat that bai.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    const confirmed = window.confirm('Ban chac chan muon vo hieu hoa tai khoan?');
    if (!confirmed) {
      return;
    }

    try {
      await api.delete('/auth/me');
      await logout();
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Khong the vo hieu hoa tai khoan.');
    }
  };

  if (loading) {
    return <div className="p-8 text-center">Dang tai thong tin tai khoan...</div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-6 pb-20 pt-10">
      <div className="mb-12">
        <h1 className="text-5xl font-black text-dark tracking-tighter uppercase leading-none">Hồ sơ cá nhân</h1>
        <p className="text-gray-400 text-xs font-black uppercase tracking-[0.2em] mt-3">Quản lý thông tin và bảo mật tài khoản của bạn</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-16">
        {/* Trái: Avatar */}
        <div className="md:col-span-1">
          <div className="bg-white rounded-[32px] shadow-sm border border-gray-100 p-10 text-center">
            <div className="relative inline-block mb-6">
              <div className="w-40 h-40 rounded-full bg-gray-50 border-8 border-white shadow-xl flex items-center justify-center text-gray-200 overflow-hidden">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-20 w-20" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                </svg>
              </div>
            </div>
            <h3 className="font-black text-2xl text-dark tracking-tight">{formData.username}</h3>
            <p className="text-gray-400 text-xs font-black uppercase tracking-widest mt-2 mb-8">{formData.email}</p>
            
            <div className="pt-6 border-t border-gray-50">
              <div className="flex justify-between items-center text-xs font-black uppercase tracking-widest text-gray-400">
                <span>Trạng thái</span>
                <span className="text-accent bg-accent/10 px-3 py-1 rounded-full">Hoạt động</span>
              </div>
            </div>
          </div>
          
          <button
            onClick={handleDelete}
            className="w-full mt-8 py-5 rounded-2xl border-2 border-dashed border-gray-100 text-gray-400 font-black uppercase tracking-widest text-xs hover:text-red-500 hover:border-red-100 transition-all"
          >
            Vô hiệu hóa tài khoản
          </button>
        </div>

        {/* Phải: Form */}
        <div className="md:col-span-2">
          <div className="bg-white rounded-[40px] shadow-[0_32px_64px_-16px_rgba(0,0,0,0.06)] p-12 border border-gray-50">
            {error && (
              <div className="bg-red-50 border-l-4 border-red-600 text-red-600 p-5 rounded-r-xl mb-10 text-xs font-black uppercase tracking-widest">
                {error}
              </div>
            )}

            <form onSubmit={handleUpdate} className="space-y-12">
              <div className="space-y-10">
                <div className="group">
                  <label className="block text-xs font-black text-gray-400 uppercase tracking-widest mb-4 group-focus-within:text-primary transition-colors">Tên người dùng hiển thị</label>
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    className="w-full pb-4 bg-transparent border-b-2 border-gray-100 focus:border-dark outline-none transition-all font-black text-xl text-dark"
                    required
                  />
                </div>

                <div className="group">
                  <label className="block text-xs font-black text-gray-400 uppercase tracking-widest mb-4 group-focus-within:text-primary transition-colors">Địa chỉ Email liên hệ</label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className="w-full pb-4 bg-transparent border-b-2 border-gray-100 focus:border-dark outline-none transition-all font-black text-xl text-dark"
                    required
                  />
                </div>
              </div>

              <div className="pt-6">
                <button
                  type="submit"
                  disabled={saving}
                  className={`px-16 py-6 bg-dark text-white rounded-[24px] font-black uppercase tracking-widest transition-all hover:bg-primary active:scale-[0.98] shadow-2xl shadow-dark/20 text-sm ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}
                >
                  {saving ? 'Đang lưu...' : 'Cập nhật hồ sơ'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;


