/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import AdminLayout from './AdminLayout';
import Notice from '../components/Notice';
import EmptyState from '../components/EmptyState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { openSignedUploadWidget } from '../utils/cloudinaryUpload';
import { getApiError } from '../utils/format';

const emptyForm = { name: '', slug: '', logoUrl: '', logoPublicId: '', description: '', active: true };

const AdminBrands = () => {
  const [brands, setBrands] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState({ type: '', text: '' });

  const fetchBrands = async () => {
    setLoading(true);
    try {
      const response = await api.get('/brands');
      setBrands(response.data || []);
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể tải thương hiệu.') });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBrands();
  }, []);

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const submitForm = async (event) => {
    event.preventDefault();
    setSaving(true);
    setNotice({ type: '', text: '' });

    try {
      if (editingId) {
        await api.put(`/brands/${editingId}`, form);
        setNotice({ type: 'success', text: 'Đã cập nhật thương hiệu.' });
      } else {
        await api.post('/brands', form);
        setNotice({ type: 'success', text: 'Đã tạo thương hiệu.' });
      }
      resetForm();
      fetchBrands();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err) });
    } finally {
      setSaving(false);
    }
  };

  const editBrand = (brand) => {
    setEditingId(brand.id);
    setForm({
      name: brand.name || '',
      slug: brand.slug || '',
      logoUrl: brand.logoUrl || '',
      logoPublicId: brand.logoPublicId || '',
      description: brand.description || '',
      active: brand.active !== false,
    });
  };

  const uploadLogo = () => {
    openSignedUploadWidget({
      folder: import.meta.env.VITE_CLOUDINARY_BRAND_FOLDER || 'ecommerce/brands',
      multiple: false,
      maxFiles: 1,
      setNotice,
      onSuccess: (info) => setForm((current) => ({
        ...current,
        logoUrl: info.secure_url,
        logoPublicId: info.public_id,
      })),
    });
  };

  const deleteBrand = async (id) => {
    if (!window.confirm('Bạn muốn xóa hoặc ẩn thương hiệu này?')) return;
    try {
      await api.delete(`/brands/${id}`);
      setNotice({ type: 'success', text: 'Đã xóa hoặc ẩn thương hiệu.' });
      fetchBrands();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể xóa thương hiệu.') });
    }
  };

  return (
    <AdminLayout title="Thương hiệu" description="Quản lý thương hiệu sản phẩm.">
      <div className="grid gap-5 lg:grid-cols-[360px_1fr]">
        <form onSubmit={submitForm} className="rounded-md border border-gray-200 bg-white p-5">
          <h2 className="font-semibold text-gray-950">{editingId ? 'Sửa thương hiệu' : 'Thêm thương hiệu'}</h2>
          <div className="mt-4 space-y-4">
            <input className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" required placeholder="Tên thương hiệu" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <input className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" placeholder="Slug, có thể để trống" value={form.slug} onChange={(e) => setForm({ ...form, slug: e.target.value })} />
            <div className="rounded-md border border-gray-200 p-3">
              <div className="flex items-center gap-3">
                <img src={form.logoUrl || 'https://via.placeholder.com/80'} alt="Logo thương hiệu" className="h-16 w-16 rounded-md object-cover" />
                <button type="button" onClick={uploadLogo} className="rounded-md bg-primary px-3 py-2 text-sm font-semibold text-white hover:bg-primary-dark">
                  Tải ảnh lên
                </button>
              </div>
              {form.logoPublicId && <p className="mt-2 truncate text-xs text-gray-500">{form.logoPublicId}</p>}
            </div>
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
          ) : brands.length === 0 ? (
            <EmptyState title="Chưa có thương hiệu" description="Tạo thương hiệu để gắn với sản phẩm." />
          ) : (
            <div className="overflow-hidden rounded-md border border-gray-200 bg-white">
              <table className="w-full text-left text-sm">
                <thead className="bg-gray-50 text-gray-500">
                  <tr>
                    <th className="px-4 py-3">Thương hiệu</th>
                    <th className="px-4 py-3">Slug</th>
                    <th className="px-4 py-3">Trạng thái</th>
                    <th className="px-4 py-3 text-right">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {brands.map((brand) => (
                    <tr key={brand.id}>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-3">
                          <img src={brand.logoUrl || 'https://via.placeholder.com/40'} alt={brand.name} className="h-10 w-10 rounded-md object-cover" />
                          <div className="font-medium text-gray-950">{brand.name}</div>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-gray-500">{brand.slug}</td>
                      <td className="px-4 py-3">{brand.active === false ? 'Đang ẩn' : 'Đang hoạt động'}</td>
                      <td className="px-4 py-3 text-right">
                        <button onClick={() => editBrand(brand)} className="mr-3 font-semibold text-primary">Sửa</button>
                        <button onClick={() => deleteBrand(brand.id)} className="font-semibold text-red-600">Xóa</button>
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

export default AdminBrands;
