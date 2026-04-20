import React, { useEffect, useState } from 'react';
import api from '../api/axios';

const AdminProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [reviewNote, setReviewNote] = useState({});

  const fetchPendingProducts = async () => {
    setLoading(true);
    try {
      const response = await api.get('/products/admin/pending');
      setProducts(response.data);
    } catch (err) {
      console.error('Failed to fetch pending products', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let active = true;

    (async () => {
      try {
        const response = await api.get('/products/admin/pending');
        if (active) {
          setProducts(response.data);
        }
      } catch {
        if (active) {
          setMessage({ text: 'Khong the tai danh sach cho duyet.', type: 'error' });
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    })();

    return () => {
      active = false;
    };
  }, []);

  const handleAction = async (productId, action) => {
    try {
      await api.put(`/products/${productId}/${action}`, { reviewNote: reviewNote[productId] || '' });
      setMessage({ text: `Đã ${action === 'approve' ? 'phê duyệt' : 'từ chối'} sản phẩm thành công!`, type: 'success' });
      fetchPendingProducts();
    } catch {
      setMessage({ text: 'Có lỗi xảy ra khi thực hiện thao tác.', type: 'error' });
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-6 pb-24 pt-10">
      <div className="mb-12">
        <h1 className="text-5xl font-black text-dark tracking-tighter uppercase leading-none">Phê duyệt sản phẩm</h1>
        <p className="text-gray-400 text-xs font-black uppercase tracking-[0.2em] mt-3">Danh sách các sản phẩm đang chờ Admin phê duyệt</p>
      </div>

      {message.text && (
        <div className={`mb-8 p-5 rounded-2xl text-xs font-black uppercase tracking-widest border-l-4 ${
          message.type === 'success' ? 'bg-green-50 border-green-500 text-green-700' : 'bg-red-50 border-red-500 text-red-700'
        }`}>
          {message.text}
        </div>
      )}

      {loading ? (
        <div className="flex justify-center py-40">
          <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : products.length === 0 ? (
        <div className="bg-white p-24 text-center rounded-[40px] shadow-sm border-2 border-dashed border-gray-100">
          <div className="text-6xl mb-6 grayscale opacity-20">📦</div>
          <h3 className="text-2xl font-black text-gray-300 uppercase tracking-widest">Không có sản phẩm chờ duyệt</h3>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6">
          {products.map((product) => (
            <div key={product.id} className="bg-white rounded-[32px] p-8 border border-gray-100 flex flex-col md:flex-row items-center gap-10 hover:shadow-xl transition-all">
              <div className="w-32 h-32 bg-gray-50 rounded-[24px] overflow-hidden flex-shrink-0">
                <img src={product.imageUrls?.[0] || 'https://via.placeholder.com/128'} alt={product.name} className="w-full h-full object-cover grayscale" />
              </div>
              
              <div className="flex-grow">
                <div className="flex items-center gap-3 mb-2">
                  <span className="bg-orange-100 text-orange-600 text-[10px] font-black px-3 py-1 rounded-full uppercase tracking-widest">PENDING</span>
                  <span className="text-[10px] font-black text-gray-300 uppercase tracking-widest">Shop: {product.shopName}</span>
                </div>
                <h3 className="text-2xl font-black text-dark tracking-tight mb-2">{product.name}</h3>
                <p className="text-gray-400 text-sm font-medium line-clamp-1 italic">"{product.description || 'Chưa có mô tả'}"</p>
                <div className="mt-4">
                  <textarea
                    rows={2}
                    value={reviewNote[product.id] || ''}
                    onChange={(e) => setReviewNote((prev) => ({ ...prev, [product.id]: e.target.value }))}
                    placeholder="Ghi chú kiểm duyệt (tuỳ chọn)"
                    className="w-full p-2 border border-gray-200 rounded-xl text-sm outline-none focus:border-primary"
                  />
                </div>
              </div>

              <div className="flex gap-4">
                <button 
                  onClick={() => handleAction(product.id, 'approve')}
                  className="bg-accent text-white px-8 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest hover:bg-emerald-600 transition-all active:scale-95 shadow-lg shadow-accent/20"
                >
                  Phê duyệt
                </button>
                <button 
                  onClick={() => handleAction(product.id, 'reject')}
                  className="bg-transparent border-2 border-red-100 text-red-400 px-8 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest hover:bg-red-50 transition-all active:scale-95"
                >
                  Từ chối
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AdminProducts;
