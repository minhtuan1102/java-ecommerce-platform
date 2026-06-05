import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import { Link } from 'react-router-dom';

const AdminProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ text: '', type: '' });

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const response = await api.get('/products', { params: { size: 100 } });
      setProducts(response.data.content || []);
    } catch (err) {
      console.error('Failed to fetch products', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let active = true;

    (async () => {
      try {
        const response = await api.get('/products', { params: { size: 100 } });
        if (active) {
          setProducts(response.data.content || []);
        }
      } catch {
        if (active) {
          setMessage({ text: 'Khong the tai danh sach san pham.', type: 'error' });
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

  const handleDelete = async (productId) => {
    if (!window.confirm('An san pham nay khoi cua hang?')) return;
    try {
      await api.delete(`/products/admin/${productId}`);
      setMessage({ text: 'Da an san pham thanh cong.', type: 'success' });
      fetchProducts();
    } catch {
      setMessage({ text: 'Khong the an san pham.', type: 'error' });
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-6 pb-24 pt-10">
      <div className="mb-12">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h1 className="text-5xl font-black text-dark tracking-tighter uppercase leading-none">Quản lý sản phẩm</h1>
            <p className="text-gray-400 text-xs font-black uppercase tracking-[0.2em] mt-3">Danh sách sản phẩm single-vendor</p>
          </div>
          <Link to="/admin/products/new" className="bg-primary text-white px-6 py-3 rounded-2xl font-black text-[10px] uppercase tracking-widest">
            Thêm sản phẩm
          </Link>
        </div>
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
          <h3 className="text-2xl font-black text-gray-300 uppercase tracking-widest">Chưa có sản phẩm</h3>
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
                  <span className={`${product.active ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-500'} text-[10px] font-black px-3 py-1 rounded-full uppercase tracking-widest`}>
                    {product.active ? 'ACTIVE' : 'INACTIVE'}
                  </span>
                  <span className="text-[10px] font-black text-gray-300 uppercase tracking-widest">{product.brandName || 'No brand'}</span>
                </div>
                <h3 className="text-2xl font-black text-dark tracking-tight mb-2">{product.name}</h3>
                <p className="text-gray-400 text-sm font-medium line-clamp-1 italic">"{product.description || 'Chưa có mô tả'}"</p>
                <p className="mt-3 text-sm font-bold text-primary">{Number(product.skus?.[0]?.price || 0).toLocaleString()} VND</p>
              </div>

              <div className="flex gap-4">
                <button 
                  onClick={() => handleDelete(product.id)}
                  className="bg-transparent border-2 border-red-100 text-red-400 px-8 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest hover:bg-red-50 transition-all active:scale-95"
                >
                  Ẩn
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
