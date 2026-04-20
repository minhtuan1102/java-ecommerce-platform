import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedSku, setSelectedSku] = useState(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await api.get(`/products/${id}`);
        setProduct(response.data);
        if (response.data.skus?.length > 0) {
          setSelectedSku(response.data.skus[0]);
        }
      } catch (err) {
        console.error('Failed to fetch product details', err);
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  const handleAddToCart = async () => {
    if (!selectedSku) return;
    try {
      await api.post('/cart', { skuId: selectedSku.id, quantity });
      alert('Đã thêm vào giỏ hàng thành công!');
    } catch (err) {
      alert(err.response?.data?.message || 'Vui lòng đăng nhập để mua hàng.');
    }
  };

  if (loading) return (
    <div className="flex justify-center py-40">
      <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
    </div>
  );

  if (!product) return (
    <div className="max-w-7xl mx-auto p-20 text-center">
      <h2 className="text-2xl font-black text-gray-300 uppercase tracking-widest text-center">Sản phẩm không tồn tại</h2>
      <button onClick={() => navigate('/')} className="mt-8 text-primary font-black uppercase tracking-widest text-xs border-b-2 border-primary">Quay lại trang chủ</button>
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto px-6 py-16 md:py-24">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-20">
        {/* Left: Product Images */}
        <div className="space-y-6">
          <div className="aspect-[4/5] bg-gray-50 rounded-[48px] overflow-hidden border border-gray-100 shadow-2xl shadow-gray-200/50">
            <img 
              src={product.imageUrls?.[0] || 'https://via.placeholder.com/600x750'} 
              alt={product.name}
              className="w-full h-full object-cover"
            />
          </div>
          <div className="grid grid-cols-4 gap-4">
            {product.imageUrls?.slice(1, 5).map((url, idx) => (
              <div key={idx} className="aspect-square bg-gray-50 rounded-2xl overflow-hidden border border-gray-100 hover:border-primary transition-colors cursor-pointer">
                <img src={url} alt={`${product.name} ${idx}`} className="w-full h-full object-cover" />
              </div>
            ))}
          </div>
        </div>

        {/* Right: Product Info */}
        <div className="flex flex-col">
          <div className="mb-10">
            <div className="flex items-center gap-4 mb-6">
              <span className="text-[10px] font-black text-primary border-2 border-primary px-3 py-1 rounded-full uppercase tracking-[0.2em]">
                {product.shopName}
              </span>
              <span className="w-1.5 h-1.5 rounded-full bg-gray-200"></span>
              <span className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">CHÍNH HÃNG</span>
            </div>
            
            <h1 className="text-5xl md:text-6xl font-black text-dark tracking-tighter uppercase leading-[0.9] mb-8">
              {product.name}
            </h1>
            
            <div className="flex items-baseline gap-2 mb-10">
              <span className="text-xs font-black text-gray-400 uppercase tracking-widest">Giá niêm yết</span>
              <div className="text-5xl font-black text-dark">
                <span className="text-2xl mr-1">₫</span>
                {selectedSku?.price?.toLocaleString() || '---'}
              </div>
            </div>

            <p className="text-gray-500 text-lg font-medium leading-relaxed italic mb-10 border-l-4 border-gray-100 pl-6">
              "{product.description || 'Sản phẩm tuyệt vời từ những nghệ nhân tâm huyết, mang lại giá trị bền vững cho người sử dụng.'}"
            </p>
          </div>

          {/* Selection */}
          <div className="space-y-10 mb-12">
            <div>
              <label className="block text-xs font-black text-gray-400 uppercase tracking-widest mb-4">Phân loại sản phẩm</label>
              <div className="flex flex-wrap gap-3">
                {product.skus?.map((sku) => (
                  <button
                    key={sku.id}
                    onClick={() => setSelectedSku(sku)}
                    className={`px-8 py-3 rounded-2xl font-black text-xs uppercase tracking-widest transition-all ${
                      selectedSku?.id === sku.id 
                      ? 'bg-dark text-white shadow-xl shadow-dark/20 scale-105' 
                      : 'bg-white text-gray-400 border-2 border-gray-100 hover:border-dark hover:text-dark'
                    }`}
                  >
                    Mẫu {sku.tierIndex}
                  </button>
                ))}
              </div>
            </div>

            <div className="flex items-center gap-10">
              <div>
                <label className="block text-xs font-black text-gray-400 uppercase tracking-widest mb-4">Số lượng</label>
                <div className="flex items-center bg-gray-50 rounded-2xl p-1 border border-gray-100">
                  <button onClick={() => setQuantity(Math.max(1, quantity - 1))} className="w-10 h-10 flex items-center justify-center font-black text-dark hover:text-primary transition-colors text-xl">－</button>
                  <span className="w-12 text-center font-black text-dark text-lg">{quantity}</span>
                  <button onClick={() => setQuantity(quantity + 1)} className="w-10 h-10 flex items-center justify-center font-black text-dark hover:text-primary transition-colors text-xl">＋</button>
                </div>
              </div>
              <div className="flex-grow">
                 <div className="text-[10px] font-black text-gray-300 uppercase tracking-widest mb-4">Kho hàng</div>
                 <div className="text-dark font-black text-sm uppercase">{selectedSku?.stock || 0} sản phẩm sẵn có</div>
              </div>
            </div>
          </div>

          <div className="mt-auto pt-8 border-t border-gray-100 flex flex-col sm:flex-row gap-4">
            <button 
              onClick={handleAddToCart}
              className="flex-1 bg-dark text-white py-6 rounded-[24px] font-black uppercase tracking-[0.2em] text-xs hover:bg-primary transition-all active:scale-[0.98] shadow-2xl shadow-dark/10"
            >
              Thêm vào giỏ hàng
            </button>
            <button className="flex-1 bg-white text-dark border-2 border-dark py-6 rounded-[24px] font-black uppercase tracking-[0.2em] text-xs hover:bg-gray-50 transition-all">
              Mua ngay lập tức
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
