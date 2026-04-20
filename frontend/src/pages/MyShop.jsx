import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

const MyShop = () => {
  const [shop, setShop] = useState(null);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchShopData = async () => {
      try {
        // Lấy thông tin Shop trước
        const shopRes = await api.get('/shops/my-shop');
        setShop(shopRes.data);

        // Sau đó mới lấy sản phẩm, nếu lỗi sản phẩm thì vẫn giữ thông tin Shop
        try {
          const productsRes = await api.get('/products/my-shop');
          setProducts(productsRes.data || []);
        } catch (pErr) {
          console.error('Lỗi khi lấy danh sách sản phẩm:', pErr);
          setProducts([]); // Mặc định là danh sách trống
        }

      } catch (err) {
        console.error('Lỗi khi lấy thông tin Shop:', err);
        setError('Không tìm thấy thông tin gian hàng. Vui lòng đảm bảo bạn đã đăng ký Shop và đang đăng nhập đúng tài khoản.');
      } finally {
        setLoading(false);
      }
    };
    fetchShopData();
  }, []);

  if (loading) return (
    <div className="flex justify-center py-40">
      <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
    </div>
  );

  return (
    <div className="max-w-6xl mx-auto p-6 pb-24 pt-10">
      {error ? (
        <div className="bg-red-50 border-l-4 border-red-600 text-red-600 p-8 rounded-r-3xl text-sm font-black uppercase tracking-widest shadow-sm">
          {error}
        </div>
      ) : (
        <div className="space-y-16">
          {/* Dashboard Header */}
          <div className="relative bg-dark rounded-[48px] p-16 overflow-hidden shadow-2xl shadow-dark/20">
            <div className="absolute top-0 right-0 w-1/2 h-full bg-gradient-to-l from-primary/20 to-transparent"></div>
            <div className="relative z-10 flex flex-col md:flex-row md:items-end justify-between gap-10">
              <div className="max-w-2xl">
                <div className="flex items-center gap-4 mb-6">
                  <span className="bg-accent text-white text-[10px] font-black px-4 py-1.5 rounded-full uppercase tracking-[0.2em] shadow-lg shadow-accent/20">
                    CỬA HÀNG {shop.status}
                  </span>
                </div>
                <h1 className="text-6xl md:text-7xl font-black text-white tracking-tighter uppercase leading-none mb-6">
                  {shop.name}
                </h1>
                <p className="text-gray-400 text-lg font-medium leading-relaxed max-w-lg italic">
                  "{shop.description || 'Chưa có mô tả cho gian hàng của bạn.'}"
                </p>
              </div>
              <div className="flex gap-4">
                <Link to="/my-shop/add-product" className="bg-white text-dark px-10 py-5 rounded-2xl font-black text-xs uppercase tracking-widest hover:bg-primary hover:text-white transition-all active:scale-95 shadow-xl shadow-white/5">
                  Thêm sản phẩm
                </Link>
                <Link to="/my-shop/orders" className="bg-transparent border-2 border-white/10 text-white px-10 py-5 rounded-2xl font-black text-xs uppercase tracking-widest hover:border-white transition-all">
                  Đơn hàng
                </Link>
              </div>
            </div>
          </div>

          {/* Product Management Section */}
          <div>
            <div className="flex items-center justify-between mb-12">
              <div>
                <h2 className="text-4xl font-black text-dark tracking-tighter uppercase leading-none">Sản phẩm của tôi</h2>
                <p className="text-gray-400 text-xs font-black uppercase tracking-[0.2em] mt-3">Quản lý kho hàng và danh mục của bạn</p>
              </div>
              <span className="text-xs font-black text-gray-300 uppercase tracking-widest">Hiển thị tất cả</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
              {/* Add New Product Card */}
              <Link to="/my-shop/add-product" className="group relative aspect-[3/4] rounded-[40px] border-4 border-dashed border-gray-100 flex flex-col items-center justify-center p-8 hover:border-primary hover:bg-primary/5 transition-all duration-500 overflow-hidden order-first">
                <div className="w-16 h-16 rounded-full bg-gray-50 flex items-center justify-center text-gray-300 group-hover:bg-primary group-hover:text-white transition-all duration-500 mb-6">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M12 4v16m8-8H4" />
                  </svg>
                </div>
                <span className="text-xs font-black text-gray-400 group-hover:text-dark uppercase tracking-widest transition-colors">Đăng bán sản phẩm mới</span>
              </Link>
              
              {/* Product Cards */}
              {products.map((product) => (
                <Link to={`/products/${product.id}`} key={product.id} className="group relative aspect-[3/4] bg-white rounded-[40px] overflow-hidden border border-gray-50 hover:shadow-2xl hover:shadow-dark/5 transition-all duration-500 block">
                  <img
                    src={product.imageUrls?.[0] || 'https://via.placeholder.com/400x500'} 
                    alt={product.name}
                    className="w-full h-full object-cover grayscale group-hover:grayscale-0 group-hover:scale-110 transition-all duration-700"
                  />
                  
                  {/* Status Badge */}
                  <div className="absolute top-6 left-6">
                    <span className={`text-[8px] font-black px-3 py-1.5 rounded-full uppercase tracking-widest shadow-lg ${
                      product.approvalStatus === 'APPROVED'
                        ? 'bg-accent text-white'
                        : product.approvalStatus === 'REJECTED'
                          ? 'bg-red-500 text-white'
                          : 'bg-orange-500 text-white'
                    }`}>
                      {product.approvalStatus === 'APPROVED'
                        ? 'Đã duyệt'
                        : product.approvalStatus === 'REJECTED'
                          ? 'Từ chối'
                          : 'Chờ duyệt'}
                    </span>
                  </div>

                  <div className="absolute inset-x-0 bottom-0 p-8 bg-gradient-to-t from-dark/90 to-transparent pt-20">
                    <h3 className="text-lg font-black text-white leading-tight mb-2 truncate">{product.name}</h3>
                    <div className="text-primary font-black text-sm">
                      <span className="text-[10px] mr-0.5">₫</span>
                      {product.skus?.[0]?.price?.toLocaleString()}
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyShop;
