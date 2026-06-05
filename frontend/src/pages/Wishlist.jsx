import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import EmptyState from '../components/EmptyState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import Notice from '../components/Notice';
import { formatMoney, getApiError } from '../utils/format';

const Wishlist = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState({ type: '', text: '' });

  useEffect(() => {
    let active = true;
    api.get('/wishlist')
      .then((response) => {
        if (active) setProducts(response.data || []);
      })
      .catch((err) => {
        if (active) setNotice({ type: 'error', text: getApiError(err, 'Không thể tải danh sách yêu thích.') });
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const removeFromWishlist = async (productId) => {
    try {
      await api.delete(`/wishlist/${productId}`);
      setProducts((current) => current.filter((product) => product.id !== productId));
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể bỏ lưu sản phẩm.') });
    }
  };

  return (
    <main className="mx-auto max-w-7xl px-4 py-6 md:px-6">
      <div className="mb-5">
        <h1 className="text-2xl font-bold text-gray-950">Sản phẩm đã lưu</h1>
        <p className="mt-1 text-sm text-gray-500">Theo dõi các sản phẩm bạn quan tâm trước khi mua.</p>
      </div>

      <Notice type={notice.type} message={notice.text} />

      {loading ? (
        <LoadingSkeleton rows={4} />
      ) : products.length === 0 ? (
        <EmptyState title="Chưa có sản phẩm đã lưu" description="Bấm biểu tượng trái tim trên sản phẩm để thêm vào danh sách này." />
      ) : (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
          {products.map((product) => {
            const sku = product.skus?.[0];
            return (
              <div key={product.id} className="overflow-hidden rounded-md border border-gray-200 bg-white">
                <Link to={`/products/${product.id}`}>
                  <div className="aspect-[4/3] bg-gray-100">
                    <img src={product.imageUrls?.[0] || 'https://via.placeholder.com/500'} alt={product.name} className="h-full w-full object-cover" />
                  </div>
                  <div className="p-3">
                    <div className="text-xs text-gray-500">{product.brandName || product.categoryName || 'Sản phẩm'}</div>
                    <h2 className="mt-1 line-clamp-2 min-h-10 text-sm font-semibold text-gray-950">{product.name}</h2>
                    <div className="mt-2 font-bold text-red-600">{formatMoney(sku?.price)}</div>
                  </div>
                </Link>
                <button onClick={() => removeFromWishlist(product.id)} className="mx-3 mb-3 w-[calc(100%-1.5rem)] rounded-md border border-red-200 px-3 py-2 text-sm font-semibold text-red-600 hover:bg-red-50">
                  Bỏ lưu
                </button>
              </div>
            );
          })}
        </div>
      )}
    </main>
  );
};

export default Wishlist;
