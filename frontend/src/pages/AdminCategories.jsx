/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useMemo, useState } from 'react';
import api from '../api/axios';
import AdminLayout from './AdminLayout';
import Notice from '../components/Notice';
import EmptyState from '../components/EmptyState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { getApiError } from '../utils/format';

const emptyForm = { name: '', slug: '', description: '', parentId: '', active: true };

const AdminCategories = () => {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState({ type: '', text: '' });

  const parentOptions = useMemo(() => categories.filter((item) => item.id !== editingId), [categories, editingId]);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const response = await api.get('/categories');
      setCategories(response.data || []);
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể tải danh mục.') });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const submitForm = async (event) => {
    event.preventDefault();
    setSaving(true);
    setNotice({ type: '', text: '' });
    const payload = {
      ...form,
      parentId: form.parentId ? Number(form.parentId) : null,
      active: Boolean(form.active),
    };

    try {
      if (editingId) {
        await api.put(`/categories/${editingId}`, payload);
        setNotice({ type: 'success', text: 'Đã cập nhật danh mục.' });
      } else {
        await api.post('/categories', payload);
        setNotice({ type: 'success', text: 'Đã tạo danh mục.' });
      }
      resetForm();
      fetchCategories();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err) });
    } finally {
      setSaving(false);
    }
  };

  const editCategory = (category) => {
    setEditingId(category.id);
    setForm({
      name: category.name || '',
      slug: category.slug || '',
      description: category.description || '',
      parentId: category.parentId || '',
      active: category.active !== false,
    });
  };

  const deleteCategory = async (id) => {
    if (!window.confirm('Bạn muốn xóa hoặc ẩn danh mục này?')) return;
    try {
      await api.delete(`/categories/${id}`);
      setNotice({ type: 'success', text: 'Đã xóa hoặc ẩn danh mục.' });
      fetchCategories();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể xóa danh mục.') });
    }
  };

  return (
    <AdminLayout title="Danh mục" description="Quản lý cây danh mục sản phẩm.">
      <div className="grid gap-5 lg:grid-cols-[360px_1fr]">
        <form onSubmit={submitForm} className="rounded-md border border-gray-200 bg-white p-5">
          <h2 className="font-semibold text-gray-950">{editingId ? 'Sửa danh mục' : 'Thêm danh mục'}</h2>
          <div className="mt-4 space-y-4">
            <input className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" required placeholder="Tên danh mục" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <input className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" placeholder="Slug, có thể để trống" value={form.slug} onChange={(e) => setForm({ ...form, slug: e.target.value })} />
            <select className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" value={form.parentId} onChange={(e) => setForm({ ...form, parentId: e.target.value })}>
              <option value="">Không có danh mục cha</option>
              {parentOptions.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
            </select>
            <textarea className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" rows="4" placeholder="Mô tả" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            <label className="flex items-center gap-2 text-sm text-gray-700">
              <input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
              Đang hoạt động
            </label>
          </div>
          <div className="mt-5 flex gap-2">
            <button disabled={saving} className="rounded-md bg-primary px-4 py-2 text-sm font-semibold text-white hover:bg-primary-dark disabled:opacity-60">
              {saving ? 'Đang lưu...' : editingId ? 'Cập nhật' : 'Tạo mới'}
            </button>
            {editingId && <button type="button" onClick={resetForm} className="rounded-md border border-gray-300 px-4 py-2 text-sm font-semibold">Hủy</button>}
          </div>
        </form>

        <div className="space-y-4">
          <Notice type={notice.type} message={notice.text} />
          {loading ? (
            <LoadingSkeleton rows={4} />
          ) : categories.length === 0 ? (
            <EmptyState title="Chưa có danh mục" description="Tạo danh mục đầu tiên để phân loại sản phẩm." />
          ) : (
            <div className="overflow-hidden rounded-md border border-gray-200 bg-white">
              <table className="w-full text-left text-sm">
                <thead className="bg-gray-50 text-gray-500">
                  <tr>
                    <th className="px-4 py-3">Tên</th>
                    <th className="px-4 py-3">Slug</th>
                    <th className="px-4 py-3">Trạng thái</th>
                    <th className="px-4 py-3 text-right">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {categories.map((category) => (
                    <tr key={category.id}>
                      <td className="px-4 py-3 font-medium text-gray-950">{category.name}</td>
                      <td className="px-4 py-3 text-gray-500">{category.slug}</td>
                      <td className="px-4 py-3">{category.active === false ? 'Đang ẩn' : 'Đang hoạt động'}</td>
                      <td className="px-4 py-3 text-right">
                        <button onClick={() => editCategory(category)} className="mr-3 font-semibold text-primary">Sửa</button>
                        <button onClick={() => deleteCategory(category.id)} className="font-semibold text-red-600">Xóa</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
  );
};

export default AdminCategories;
