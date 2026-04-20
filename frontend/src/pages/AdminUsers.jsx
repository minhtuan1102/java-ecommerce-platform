import React, { useEffect, useState } from 'react';
import api from '../api/axios';

const ROLE_OPTIONS = ['ROLE_USER', 'ROLE_SELLER', 'ROLE_ADMIN'];

const AdminUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingUserId, setEditingUserId] = useState(null);
  const [editForm, setEditForm] = useState({ username: '', email: '', roles: ['ROLE_USER'] });

  const fetchUsers = async () => {
    try {
      const response = await api.get('/auth/users');
      setUsers(response.data);
    } catch (err) {
      alert(err.response?.data?.message || 'Khong the tai danh sach user.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    (async () => {
      await fetchUsers();
    })();
  }, []);

  const startEdit = (user) => {
    setEditingUserId(user.id);
    setEditForm({
      username: user.username,
      email: user.email,
      roles: user.roles || ['ROLE_USER']
    });
  };

  const toggleRole = (roleName) => {
    setEditForm((prev) => {
      const hasRole = prev.roles.includes(roleName);
      let nextRoles;

      if (hasRole) {
        nextRoles = prev.roles.filter((r) => r !== roleName);
      } else {
        nextRoles = [...prev.roles, roleName];
      }

      if (nextRoles.length === 0) {
        nextRoles = ['ROLE_USER'];
      }

      return { ...prev, roles: nextRoles };
    });
  };

  const handleSave = async (userId) => {
    try {
      await api.put(`/auth/users/${userId}`, editForm);
      setEditingUserId(null);
      await fetchUsers();
      alert('Cap nhat user thanh cong.');
    } catch (err) {
      alert(err.response?.data?.message || 'Cap nhat user that bai.');
    }
  };

  const handleDeactivate = async (userId) => {
    const confirmed = window.confirm('Vo hieu hoa user nay?');
    if (!confirmed) {
      return;
    }

    try {
      await api.delete(`/auth/users/${userId}`);
      await fetchUsers();
      alert('User da duoc vo hieu hoa.');
    } catch (err) {
      alert(err.response?.data?.message || 'Khong the vo hieu hoa user.');
    }
  };

  if (loading) {
    return <div className="p-8 text-center">Dang tai danh sach user...</div>;
  }

  return (
    <div className="max-w-6xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6 text-gray-800">Quan ly nguoi dung</h1>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>
              <th className="p-4">ID</th>
              <th className="p-4">Username</th>
              <th className="p-4">Email</th>
              <th className="p-4">Roles</th>
              <th className="p-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className="border-t border-gray-100">
                <td className="p-4">#{user.id}</td>
                <td className="p-4">
                  {editingUserId === user.id ? (
                    <input
                      value={editForm.username}
                      onChange={(e) => setEditForm((prev) => ({ ...prev, username: e.target.value }))}
                      className="w-full p-2 border rounded"
                    />
                  ) : user.username}
                </td>
                <td className="p-4">
                  {editingUserId === user.id ? (
                    <input
                      value={editForm.email}
                      onChange={(e) => setEditForm((prev) => ({ ...prev, email: e.target.value }))}
                      className="w-full p-2 border rounded"
                    />
                  ) : user.email}
                </td>
                <td className="p-4">
                  {editingUserId === user.id ? (
                    <div className="flex flex-wrap gap-2">
                      {ROLE_OPTIONS.map((role) => (
                        <label key={role} className="text-xs flex items-center gap-1 border px-2 py-1 rounded">
                          <input
                            type="checkbox"
                            checked={editForm.roles.includes(role)}
                            onChange={() => toggleRole(role)}
                          />
                          {role}
                        </label>
                      ))}
                    </div>
                  ) : (
                    <span>{(user.roles || []).join(', ')}</span>
                  )}
                </td>
                <td className="p-4 text-center space-x-3">
                  {editingUserId === user.id ? (
                    <>
                      <button onClick={() => handleSave(user.id)} className="text-blue-600 hover:underline">Save</button>
                      <button onClick={() => setEditingUserId(null)} className="text-gray-500 hover:underline">Cancel</button>
                    </>
                  ) : (
                    <>
                      <button onClick={() => startEdit(user)} className="text-blue-600 hover:underline">Edit</button>
                      <button onClick={() => handleDeactivate(user.id)} className="text-red-600 hover:underline">Deactivate</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminUsers;


