import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

const Home = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState([]);
  
  // Search & Filter States
  const [searchName, setSearchName] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (searchName) params.append('name', searchName);
      if (selectedCategory) params.append('categoryId', selectedCategory);
      if (minPrice) params.append('minPrice', minPrice);
      if (maxPrice) params.append('maxPrice', maxPrice);

      const response = await api.get(`/products?${params.toString()}`);
      setProducts(response.data);
    } catch (err) {
      console.error('Failed to fetch products', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let active = true;

    (async () => {
      setLoading(true);
      try {
        const [categoriesResponse, productsResponse] = await Promise.all([
          api.get('/categories'),
          api.get('/products')
        ]);

        if (!active) {
          return;
        }

        setCategories(categoriesResponse.data);
        setProducts(productsResponse.data);
      } catch (err) {
        console.error('Failed to load home data', err);
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

  const handleSearch = (e) => {
    e.preventDefault();
    fetchProducts();
  };

  const handleAddToCart = async (e, skuId) => {
    e.preventDefault();
    e.stopPropagation();

    try {
      await api.post('/cart', { skuId, quantity: 1 });
      alert('Đã thêm sản phẩm vào giỏ hàng!');
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể thêm vào giỏ hàng. Vui lòng đăng nhập.');
    }
  };

  return (
    <div className="pb-24">
      {/* Editorial Header */}
      <div className="max-w-7xl mx-auto px-6 pt-16 pb-12">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-8">
          <div className="max-w-2xl">
            <h1 className="text-6xl md:text-8xl font-black text-dark tracking-tighter leading-[0.9] uppercase">
              CHUẨN MỰC <br /> <span className="text-primary italic">MỚI.</span>
            </h1>
            <p className="mt-8 text-gray-500 font-medium text-lg max-w-md">
              Bộ sưu tập được tuyển chọn kỹ lưỡng cho những người trân trọng giá trị hơn số lượng.
            </p>
          </div>
          
          <div className="flex flex-col items-end gap-4">
            <div className="flex bg-white p-1.5 rounded-2xl shadow-sm border border-gray-100">
              <input 
                type="text" 
                placeholder="Tìm sản phẩm..." 
                className="bg-transparent px-4 py-2 outline-none text-sm font-bold w-48 md:w-64"
                value={searchName}
                onChange={(e) => setSearchName(e.target.value)}
              />
              <button 
                onClick={handleSearch}
                className="bg-dark text-white p-2.5 rounded-xl hover:bg-primary transition-colors"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </button>
            </div>
            
            <div className="flex gap-2">
              <select 
                className="bg-transparent text-[10px] font-black uppercase tracking-widest border-b-2 border-dark pb-1 outline-none cursor-pointer"
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
              >
                <option value="">Tất cả danh mục</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              <input
                type="number"
                min="0"
                placeholder="Giá từ"
                value={minPrice}
                onChange={(e) => setMinPrice(e.target.value)}
                className="bg-transparent text-[10px] font-black uppercase tracking-widest border-b-2 border-dark pb-1 outline-none w-24"
              />
              <input
                type="number"
                min="0"
                placeholder="Đến"
                value={maxPrice}
                onChange={(e) => setMaxPrice(e.target.value)}
                className="bg-transparent text-[10px] font-black uppercase tracking-widest border-b-2 border-dark pb-1 outline-none w-20"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Product Feed */}
      <div className="max-w-7xl mx-auto px-6">
        <div className="h-px bg-gray-100 w-full mb-12"></div>
        
        {loading ? (
          <div className="flex justify-center py-40">
            <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : products.length === 0 ? (
          <div className="text-center py-40">
            <h3 className="text-2xl font-black text-gray-300 uppercase tracking-widest italic">Bộ sưu tập trống</h3>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-x-8 gap-y-16">
            {products.map((product) => (
              <Link to={`/products/${product.id}`} key={product.id} className="group cursor-pointer block">
                <div className="relative aspect-[4/5] bg-gray-100 rounded-[32px] overflow-hidden mb-6">
                  <img
                    src={product.imageUrls?.[0] || 'https://via.placeholder.com/400x500'} 
                    alt={product.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-all duration-700 ease-out"
                  />
                  <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center">
                    <button 
                      onClick={(e) => product.skus?.[0] && handleAddToCart(e, product.skus[0].id)}
                      className="bg-white text-dark px-8 py-3 rounded-full font-black text-[10px] uppercase tracking-widest translate-y-4 group-hover:translate-y-0 transition-transform duration-500"
                    >
                      Thêm vào túi
                    </button>
                  </div>
                </div>
                
                <div className="flex justify-between items-start gap-4 px-2">
                  <div className="flex-grow">
                    <h3 className="text-lg font-black text-dark leading-tight mb-2 group-hover:text-primary transition-colors">
                      {product.name}
                    </h3>
                    <div className="flex items-center gap-3">
                      <span className="text-xs font-black text-gray-400 uppercase tracking-widest">{product.shopName}</span>
                      <span className="w-1.5 h-1.5 rounded-full bg-gray-200"></span>
                      <span className="text-xs font-black text-accent uppercase tracking-widest">Sẵn có</span>
                    </div>
                  </div>
                  <div className="text-right flex-shrink-0">
                    <div className="text-xl font-black text-dark">
                      <span className="text-sm mr-0.5">₫</span>
                      {product.skus?.[0]?.price?.toLocaleString()}
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Home;
