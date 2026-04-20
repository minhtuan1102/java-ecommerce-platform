import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';

const ShopDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [shop, setShop] = useState(null);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const pageSize = 20;

  const fetchShopProducts = async (page = 0) => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.append('shopId', id);
      params.append('page', page);
      params.append('size', pageSize);

      const response = await api.get(`/products?${params.toString()}`);
      
      if (response.data.content) {
        setProducts(response.data.content);
        setTotalPages(response.data.totalPages);
      } else {
        setProducts(response.data);
        setTotalPages(Math.ceil(response.data.length / pageSize) || 1);
      }
    } catch (err) {
      console.error('Failed to fetch shop products', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const fetchShopInfo = async () => {
      try {
        const shopRes = await api.get(`/shops/${id}`);
        setShop(shopRes.data);
        await fetchShopProducts(0);
      } catch (err) {
        console.error('Failed to fetch shop data', err);
      }
    };
    fetchShopInfo();
  }, [id]);

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
    fetchShopProducts(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  if (loading && !shop) return (
    <div className="flex justify-center py-40">
      <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
    </div>
  );

  if (!shop) return <div className="text-center py-40 font-black text-gray-300 uppercase tracking-[0.2em]">Không tìm thấy gian hàng</div>;

  return (
    <div className="pb-24">
      {/* Shop Hero */}
      <div className="bg-dark text-white pt-24 pb-16 px-6">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center gap-10">
          <div className="w-32 h-32 rounded-full bg-primary flex items-center justify-center text-4xl font-black shadow-2xl shadow-primary/20 flex-shrink-0">
            {shop.name?.[0]}
          </div>
          <div>
            <div className="flex items-center gap-4 mb-4">
              <span className="bg-accent text-white text-[10px] font-black px-3 py-1 rounded-full uppercase tracking-widest shadow-lg shadow-accent/20">Cửa hàng uy tín</span>
              <span className="text-gray-400 text-[10px] font-black uppercase tracking-widest italic tracking-widest">Hoạt động từ 2024</span>
            </div>
            <h1 className="text-5xl md:text-6xl font-black tracking-tighter uppercase mb-4 leading-none">{shop.name}</h1>
            <p className="text-gray-400 text-lg max-w-2xl italic leading-relaxed">"{shop.description || 'Chuyên cung cấp các sản phẩm chất lượng cao, phục vụ tận tâm.'}"</p>
          </div>
        </div>
      </div>

      {/* Product List */}
      <div className="max-w-7xl mx-auto px-6 mt-16">
        <div className="flex items-center justify-between mb-12 border-b border-gray-100 pb-8">
          <div>
            <h2 className="text-4xl font-black text-dark tracking-tighter uppercase leading-none">Danh sách sản phẩm</h2>
            <p className="text-gray-400 text-xs font-black uppercase tracking-widest mt-3">Khám phá tất cả mặt hàng của cửa hàng</p>
          </div>
        </div>

        {loading ? (
          <div className="flex justify-center py-20">
            <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : products.length === 0 ? (
          <div className="text-center py-20 font-black text-gray-200 uppercase tracking-widest italic">Cửa hàng chưa có sản phẩm nào</div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-x-8 gap-y-16">
              {products.map((product) => (
                <Link to={`/products/${product.id}`} key={product.id} className="group block">
                  <div className="relative aspect-[4/5] bg-gray-50 rounded-[32px] overflow-hidden mb-6 border border-gray-100">
                    <img 
                      src={product.imageUrls?.[0] || 'https://via.placeholder.com/400x500?text=No+Image'} 
                      alt={product.name}
                      className="w-full h-full object-cover group-hover:scale-105 transition-all duration-700 ease-out grayscale group-hover:grayscale-0"
                      onError={(e) => {
                        e.target.src = 'https://via.placeholder.com/400x500?text=Link+Ảnh+Lỗi';
                        e.target.onerror = null;
                      }}
                    />
                  </div>
                  <h3 className="text-lg font-black text-dark leading-tight mb-2 group-hover:text-primary transition-colors">{product.name}</h3>
                  <div className="text-2xl font-black text-dark">
                    <span className="text-sm mr-0.5">₫</span>
                    {product.skus?.[0]?.price?.toLocaleString()}
                  </div>
                </Link>
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-20 flex justify-center items-center gap-4">
                <button 
                  disabled={currentPage === 0}
                  onClick={() => handlePageChange(currentPage - 1)}
                  className="w-12 h-12 rounded-2xl border-2 border-gray-100 flex items-center justify-center font-black text-dark hover:border-dark disabled:opacity-30 transition-all"
                >
                  ←
                </button>
                <div className="flex gap-2">
                  {[...Array(totalPages)].map((_, i) => (
                    <button
                      key={i}
                      onClick={() => handlePageChange(i)}
                      className={`w-12 h-12 rounded-2xl font-black text-xs transition-all ${
                        currentPage === i ? 'bg-dark text-white shadow-xl shadow-dark/20 scale-110' : 'bg-white text-gray-400 border-2 border-gray-100 hover:border-dark'
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}
                </div>
                <button 
                  disabled={currentPage === totalPages - 1}
                  onClick={() => handlePageChange(currentPage + 1)}
                  className="w-12 h-12 rounded-2xl border-2 border-gray-100 flex items-center justify-center font-black text-dark hover:border-dark disabled:opacity-30 transition-all"
                >
                  →
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default ShopDetail;
