import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Notice from '../components/Notice';
import { useAuth } from '../context/AuthContext';
import { openSignedUploadWidget } from '../utils/cloudinaryUpload';
import { getApiError } from '../utils/format';

const Profile = () => {
  const { updateUser, logout } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ username: '', email: '', avatarUrl: '', avatarPublicId: '' });
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [notice, setNotice] = useState({ type: '', text: '' });

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const response = await api.get('/auth/me');
        if (!active) return;
        setFormData({
          username: response.data.username || '',
          email: response.data.email || '',
          avatarUrl: response.data.avatarUrl || '',
          avatarPublicId: response.data.avatarPublicId || '',
        });
        updateUser({
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
          avatarUrl: response.data.avatarUrl,
          avatarPublicId: response.data.avatarPublicId,
          roles: response.data.roles,
        });
      } catch (err) {
        if (active) setNotice({ type: 'error', text: getApiError(err, 'Không thể tải thông tin tài khoản.') });
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, [updateUser]);

  const updateProfile = async (event) => {
    event.preventDefault();
    setSaving(true);
    setNotice({ type: '', text: '' });
    try {
      const response = await api.put('/auth/me', formData);
      updateUser({
        id: response.data.id,
        username: response.data.username,
        email: response.data.email,
        avatarUrl: response.data.avatarUrl,
        avatarPublicId: response.data.avatarPublicId,
        roles: response.data.roles,
      });
      setNotice({ type: 'success', text: 'Đã cập nhật hồ sơ.' });
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể cập nhật hồ sơ.') });
    } finally {
      setSaving(false);
    }
  };

  const uploadAvatar = () => {
    openSignedUploadWidget({
      folder: import.meta.env.VITE_CLOUDINARY_AVATAR_FOLDER || 'ecommerce/avatars',
      multiple: false,
      maxFiles: 1,
      setNotice,
      onSuccess: async (info) => {
        const nextData = {
          ...formData,
          avatarUrl: info.secure_url,
          avatarPublicId: info.public_id,
        };
        setFormData(nextData);
        setSaving(true);
        try {
          const response = await api.put('/auth/me', nextData);
          updateUser({
            id: response.data.id,
            username: response.data.username,
            email: response.data.email,
            avatarUrl: response.data.avatarUrl,
            avatarPublicId: response.data.avatarPublicId,
            roles: response.data.roles,
          });
          setFormData({
            username: response.data.username || '',
            email: response.data.email || '',
            avatarUrl: response.data.avatarUrl || '',
            avatarPublicId: response.data.avatarPublicId || '',
          });
          setNotice({ type: 'success', text: 'Đã cập nhật ảnh đại diện.' });
        } catch (err) {
          setNotice({ type: 'error', text: getApiError(err, 'Không thể lưu ảnh đại diện.') });
        } finally {
          setSaving(false);
        }
      },
    });
  };

  const changePassword = async (event) => {
    event.preventDefault();
    setSavingPassword(true);
    setNotice({ type: '', text: '' });
    try {
      await api.post('/auth/me/change-password', passwordForm);
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setNotice({ type: 'success', text: 'Đã đổi mật khẩu. Vui lòng đăng nhập lại.' });
      await logout();
      navigate('/login');
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể đổi mật khẩu.') });
    } finally {
      setSavingPassword(false);
    }
  };

  const deactivateAccount = async () => {
    if (!window.confirm('Bạn muốn vô hiệu hóa tài khoản này?')) return;
    try {
      await api.delete('/auth/me');
      await logout();
      navigate('/login');
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể vô hiệu hóa tài khoản.') });
    }
  };

  return (
    <main className="mx-auto max-w-4xl px-4 py-6 md:px-6">
      <div className="mb-5">
        <h1 className="text-2xl font-bold text-gray-950">Tài khoản của tôi</h1>
        <p className="mt-1 text-sm text-gray-500">Cập nhật thông tin đăng nhập và hồ sơ cơ bản.</p>
      </div>

      <Notice type={notice.type} message={notice.text} />

      {loading ? (
        <div className="mt-4 rounded-md border border-gray-200 bg-white p-5 text-sm text-gray-500">Đang tải thông tin tài khoản...</div>
      ) : (
        <div className="mt-4 grid gap-5 md:grid-cols-[260px_1fr]">
          <aside className="rounded-md border border-gray-200 bg-white p-5">
            {formData.avatarUrl ? (
              <img src={formData.avatarUrl} alt={formData.username || 'Tài khoản'} className="h-20 w-20 rounded-full object-cover" />
            ) : (
              <div className="flex h-20 w-20 items-center justify-center rounded-full bg-primary text-2xl font-bold text-white">
                {(formData.username || formData.email || 'U').slice(0, 1).toUpperCase()}
              </div>
            )}
            <h2 className="mt-4 font-bold text-gray-950">{formData.username}</h2>
            <p className="mt-1 break-all text-sm text-gray-500">{formData.email}</p>
            <button onClick={deactivateAccount} className="mt-5 rounded-md border border-red-200 px-4 py-2 text-sm font-semibold text-red-600">
              Vô hiệu hóa tài khoản
            </button>
          </aside>

          <div className="space-y-5">
            <form onSubmit={updateProfile} className="rounded-md border border-gray-200 bg-white p-5">
              <h2 className="font-bold text-gray-950">Thông tin cá nhân</h2>
              <div className="mt-4 space-y-4">
                <div>
                  <label className="text-sm font-semibold text-gray-700">Tên người dùng</label>
                  <input required name="username" value={formData.username} onChange={(e) => setFormData({ ...formData, username: e.target.value })} className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
                </div>
                <div>
                  <label className="text-sm font-semibold text-gray-700">Email</label>
                  <input required type="email" name="email" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
                </div>
                <div className="rounded-md border border-gray-200 p-3">
                  <label className="text-sm font-semibold text-gray-700">Ảnh đại diện</label>
                  <div className="mt-3 flex items-center gap-3">
                    {formData.avatarUrl ? (
                      <img src={formData.avatarUrl} alt="Ảnh đại diện" className="h-16 w-16 rounded-full object-cover" />
                    ) : (
                      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gray-100 text-xl font-bold text-gray-500">
                        {(formData.username || formData.email || 'U').slice(0, 1).toUpperCase()}
                      </div>
                    )}
                    <div>
                      <button type="button" onClick={uploadAvatar} className="rounded-md bg-primary px-3 py-2 text-sm font-semibold text-white hover:bg-primary-dark">
                        Tải ảnh lên
                      </button>
                      <p className="mt-1 text-xs text-gray-500">Ảnh sẽ được lưu ngay sau khi tải lên.</p>
                    </div>
                  </div>
                  {formData.avatarPublicId && <p className="mt-2 truncate text-xs text-gray-500">{formData.avatarPublicId}</p>}
                </div>
              </div>
              <button disabled={saving} className="mt-5 rounded-md bg-primary px-4 py-2 text-sm font-semibold text-white hover:bg-primary-dark disabled:opacity-60">
                {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
              </button>
            </form>

            <form onSubmit={changePassword} className="rounded-md border border-gray-200 bg-white p-5">
              <h2 className="font-bold text-gray-950">Đổi mật khẩu</h2>
              <div className="mt-4 space-y-4">
                <div>
                  <label className="text-sm font-semibold text-gray-700">Mật khẩu hiện tại</label>
                  <input required type="password" value={passwordForm.currentPassword} onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })} className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
                </div>
                <div>
                  <label className="text-sm font-semibold text-gray-700">Mật khẩu mới</label>
                  <input required minLength="6" type="password" value={passwordForm.newPassword} onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })} className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
                </div>
                <div>
                  <label className="text-sm font-semibold text-gray-700">Nhập lại mật khẩu mới</label>
                  <input required minLength="6" type="password" value={passwordForm.confirmPassword} onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })} className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
                </div>
              </div>
              <button disabled={savingPassword} className="mt-5 rounded-md bg-gray-950 px-4 py-2 text-sm font-semibold text-white hover:bg-primary disabled:opacity-60">
                {savingPassword ? 'Đang đổi...' : 'Đổi mật khẩu'}
              </button>
            </form>
          </div>
        </div>
      )}
    </main>
  );
};

export default Profile;
