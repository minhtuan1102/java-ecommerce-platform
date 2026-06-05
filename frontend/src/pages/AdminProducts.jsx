/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import AdminLayout from './AdminLayout';
import Notice from '../components/Notice';
import EmptyState from '../components/EmptyState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { formatMoney, getApiError } from '../utils/format';

const AdminProducts = () => {
  const [products, setProducts] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState({ type: '', text: '' });

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const response = await api.get('/products', { params: { name: keyword || undefined, size: 100 } });
      setProducts(response.data.content || []);
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể tải sản phẩm.') });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const hideProduct = async (productId) => {
    if (!window.confirm('Bạn muốn ẩn sản phẩm này khỏi cửa hàng?')) return;
    try {
      await api.delete(`/products/admin/${productId}`);
      setNotice({ type: 'success', text: 'Đã ẩn sản phẩm.' });
      fetchProducts();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể ẩn sản phẩm.') });
    }
  };

  return (
    <AdminLayout
      title="Sản phẩm"
      description="Tạo, sửa và ẩn sản phẩm single-vendor."
      action={<Link to="/admin/products/new" className="rounded-md bg-primary px-4 py-2 text-sm font-semibold text-white hover:bg-primary-dark">Thêm sản phẩm</Link>}
    >
      <div className="space-y-4">
        <form
          onSubmit={(event) => {
            event.preventDefault();
            fetchProducts();
          }}
          className="flex flex-col gap-3 rounded-md border border-gray-200 bg-white p-4 md:flex-row"
        >
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Tìm theo tên sản phẩm" className="min-w-0 flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm" />
          <button className="rounded-md border border-gray-300 px-4 py-2 text-sm font-semibold hover:bg-gray-50">Tìm kiếm</button>
        </form>

        <Notice type={notice.type} message={notice.text} />

        {loading ? (
          <LoadingSkeleton rows={5} />
        ) : products.length === 0 ? (
          <EmptyState title="Chưa có sản phẩm" description="Tạo sản phẩm đầu tiên để bán trên cửa hàng." action={<Link to="/admin/products/new" className="rounded-md bg-primary px-4 py-2 text-sm font-semibold text-white">Thêm sản phẩm</Link>} />
        ) : (
          <div className="overflow-hidden rounded-md border border-gray-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="bg-gray-50 text-gray-500">
                <tr>
                  <th className="px-4 py-3">Sản phẩm</th>
                  <th className="px-4 py-3">Danh mục</th>
                  <th className="px-4 py-3">Giá</th>
                  <th className="px-4 py-3">Kho</th>
                  <th className="px-4 py-3">Trạng thái</th>
                  <th className="px-4 py-3 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {products.map((product) => {
                  const stock = product.skus?.reduce((sum, sku) => sum + Number(sku.stock || 0), 0) || 0;
                  const price = product.skus?.[0]?.price || 0;
                  return (
                    <tr key={product.id}>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-3">
                          <img src={product.imageUrls?.[0] || 'https://via.placeholder.com/56'} alt={product.name} className="h-14 w-14 rounded-md object-cover" />
                          <div>
                            <div className="font-semibold text-gray-950">{product.name}</div>
                            <div className="text-xs text-gray-500">{product.brandName || 'Chưa có thương hiệu'}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-gray-600">{product.categoryName || 'Chưa phân loại'}</td>
                      <td className="px-4 py-3 font-semibold">{formatMoney(price)}</td>
                      <td className="px-4 py-3">{stock}</td>
                      <td className="px-4 py-3">{product.active ? 'Đang bán' : 'Đang ẩn'}</td>
                      <td className="px-4 py-3 text-right">
                        <Link to={`/admin/products/${product.id}/edit`} className="mr-3 font-semibold text-primary">Sửa</Link>
                        <button onClick={() => hideProduct(product.id)} className="font-semibold text-red-600">Ẩn</button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminProducts;
