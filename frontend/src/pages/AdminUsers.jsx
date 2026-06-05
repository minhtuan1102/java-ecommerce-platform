/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import AdminLayout from './AdminLayout';
import Notice from '../components/Notice';
import EmptyState from '../components/EmptyState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { getApiError } from '../utils/format';

const ROLE_OPTIONS = [
  { value: 'ROLE_CUSTOMER', label: 'Khách hàng' },
  { value: 'ROLE_ADMIN', label: 'Quản trị viên' },
];

const roleLabel = (role) => ROLE_OPTIONS.find((item) => item.value === role)?.label || role;

const AdminUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingUserId, setEditingUserId] = useState(null);
  const [editForm, setEditForm] = useState({ username: '', email: '', roles: ['ROLE_CUSTOMER'] });
  const [notice, setNotice] = useState({ type: '', text: '' });

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await api.get('/auth/users');
      setUsers(response.data || []);
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể tải người dùng.') });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const startEdit = (user) => {
    setEditingUserId(user.id);
    setEditForm({ username: user.username || '', email: user.email || '', roles: user.roles?.length ? user.roles : ['ROLE_CUSTOMER'] });
  };

  const toggleRole = (roleName) => {
    setEditForm((prev) => {
      const nextRoles = prev.roles.includes(roleName)
        ? prev.roles.filter((role) => role !== roleName)
        : [...prev.roles, roleName];
      return { ...prev, roles: nextRoles.length ? nextRoles : ['ROLE_CUSTOMER'] };
    });
  };

  const saveUser = async (userId) => {
    try {
      await api.put(`/auth/users/${userId}`, editForm);
      setNotice({ type: 'success', text: 'Đã cập nhật người dùng.' });
      setEditingUserId(null);
      fetchUsers();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể cập nhật người dùng.') });
    }
  };

  const deactivateUser = async (userId) => {
    if (!window.confirm('Bạn muốn vô hiệu hóa người dùng này?')) return;
    try {
      await api.delete(`/auth/users/${userId}`);
      setNotice({ type: 'success', text: 'Đã vô hiệu hóa người dùng.' });
      fetchUsers();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể vô hiệu hóa người dùng.') });
    }
  };

  return (
    <AdminLayout title="Người dùng" description="Quản lý tài khoản khách hàng và quản trị viên.">
      <div className="space-y-4">
        <Notice type={notice.type} message={notice.text} />
        {loading ? (
          <LoadingSkeleton rows={5} />
        ) : users.length === 0 ? (
          <EmptyState title="Chưa có người dùng" description="Tài khoản mới sẽ xuất hiện tại đây." />
        ) : (
          <div className="overflow-hidden rounded-md border border-gray-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="bg-gray-50 text-gray-500">
                <tr>
                  <th className="px-4 py-3">ID</th>
                  <th className="px-4 py-3">Tên người dùng</th>
                  <th className="px-4 py-3">Email</th>
                  <th className="px-4 py-3">Vai trò</th>
                  <th className="px-4 py-3 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {users.map((user) => (
                  <tr key={user.id}>
                    <td className="px-4 py-3">#{user.id}</td>
                    <td className="px-4 py-3">
                      {editingUserId === user.id ? (
                        <input value={editForm.username} onChange={(e) => setEditForm({ ...editForm, username: e.target.value })} className="w-full rounded-md border border-gray-300 px-3 py-2" />
                      ) : (
                        <div className="flex items-center gap-3">
                          {user.avatarUrl ? (
                            <img src={user.avatarUrl} alt={user.username || 'Người dùng'} className="h-9 w-9 rounded-full object-cover" />
                          ) : (
                            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gray-100 text-sm font-bold text-gray-500">
                              {(user.username || user.email || 'U').slice(0, 1).toUpperCase()}
                            </div>
                          )}
                          <span className="font-medium text-gray-950">{user.username}</span>
                        </div>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      {editingUserId === user.id ? (
                        <input value={editForm.email} onChange={(e) => setEditForm({ ...editForm, email: e.target.value })} className="w-full rounded-md border border-gray-300 px-3 py-2" />
                      ) : user.email}
                    </td>
                    <td className="px-4 py-3">
                      {editingUserId === user.id ? (
                        <div className="flex flex-wrap gap-2">
                          {ROLE_OPTIONS.map((role) => (
                            <label key={role.value} className="flex items-center gap-2 rounded-md border border-gray-300 px-2 py-1 text-xs">
                              <input type="checkbox" checked={editForm.roles.includes(role.value)} onChange={() => toggleRole(role.value)} />
                              {role.label}
                            </label>
                          ))}
                        </div>
                      ) : (
                        <span>{(user.roles || []).map(roleLabel).join(', ')}</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {editingUserId === user.id ? (
                        <>
                          <button onClick={() => saveUser(user.id)} className="mr-3 font-semibold text-primary">Lưu</button>
                          <button onClick={() => setEditingUserId(null)} className="font-semibold text-gray-600">Hủy</button>
                        </>
                      ) : (
                        <>
                          <button onClick={() => startEdit(user)} className="mr-3 font-semibold text-primary">Sửa</button>
                          <button onClick={() => deactivateUser(user.id)} className="font-semibold text-red-600">Vô hiệu hóa</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminUsers;
