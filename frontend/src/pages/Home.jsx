import React, { useEffect, useState } from 'react';
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

  const handleAddToCart = async (skuId) => {
    try {
      await api.post('/cart', { skuId, quantity: 1 });
      alert('Đã thêm sản phẩm vào giỏ hàng!');
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể thêm vào giỏ hàng. Vui lòng đăng nhập.');
    }
  };

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Search Bar Section */}
      <div className="bg-white p-4 rounded-lg shadow-sm border mb-8 flex flex-wrap gap-4 items-end">
        <div className="flex-1 min-w-[200px]">
          <label className="block text-xs font-bold text-gray-400 uppercase mb-1">Tìm kiếm tên</label>
          <input 
            type="text" 
            placeholder="Bạn đang tìm gì hôm nay?" 
            className="w-full p-2 border rounded outline-none focus:border-primary"
            value={searchName}
            onChange={(e) => setSearchName(e.target.value)}
          />
        </div>
        <div className="w-48">
          <label className="block text-xs font-bold text-gray-400 uppercase mb-1">Danh mục</label>
          <select 
            className="w-full p-2 border rounded outline-none bg-white"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
          >
            <option value="">Tất cả</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div className="w-32">
          <label className="block text-xs font-bold text-gray-400 uppercase mb-1">Giá từ</label>
          <input 
            type="number" 
            placeholder="₫" 
            className="w-full p-2 border rounded outline-none"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
          />
        </div>
        <div className="w-32">
          <label className="block text-xs font-bold text-gray-400 uppercase mb-1">Đến</label>
          <input 
            type="number" 
            placeholder="₫" 
            className="w-full p-2 border rounded outline-none"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
          />
        </div>
        <button 
          onClick={handleSearch}
          className="bg-primary text-white px-8 py-2 rounded font-bold hover:bg-red-600 transition"
        >
          TÌM KIẾM
        </button>
      </div>

      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold text-gray-800 uppercase tracking-tight">Danh sách sản phẩm</h2>
        <span className="text-gray-400 text-sm">Tìm thấy {products.length} sản phẩm</span>
      </div>
      
      {loading ? (
        <div className="text-center py-20 italic text-gray-400">Đang tìm sản phẩm phù hợp...</div>
      ) : products.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-lg border border-dashed text-gray-400">
          Không tìm thấy sản phẩm nào khớp với yêu cầu của bạn.
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-4">
          {products.map((product) => (
            <div key={product.id} className="bg-white rounded-lg shadow-sm border border-gray-100 hover:shadow-md transition overflow-hidden group flex flex-col h-full">
              <div className="relative pt-[100%] bg-gray-50">
                <img 
                  src={product.imageUrls?.[0] || 'https://via.placeholder.com/300'} 
                  alt={product.name}
                  className="absolute top-0 left-0 w-full h-full object-cover group-hover:scale-105 transition duration-300"
                />
              </div>
              <div className="p-3 flex flex-col flex-grow">
                <h3 className="text-sm text-gray-700 line-clamp-2 mb-2 min-h-[40px] leading-tight font-medium">
                  {product.name}
                </h3>
                <div className="mt-auto">
                  <div className="flex items-baseline gap-1 text-primary font-bold text-lg">
                    <span className="text-xs">₫</span>
                    {product.skus?.[0]?.price?.toLocaleString() || '---'}
                  </div>
                  <div className="text-[10px] text-gray-400 mt-1 flex justify-between">
                    <span>{product.shopName}</span>
                    <span>Hà Nội</span>
                  </div>
                  <button 
                    onClick={() => product.skus?.[0] && handleAddToCart(product.skus[0].id)}
                    disabled={!product.skus || product.skus.length === 0}
                    className={`w-full mt-3 border py-1.5 rounded text-xs font-bold transition uppercase ${
                      product.skus?.[0] 
                      ? 'bg-primary/10 text-primary border-primary/20 hover:bg-primary hover:text-white' 
                      : 'bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed'
                    }`}
                  >
                    {product.skus?.[0] ? 'Thêm vào giỏ' : 'Hết hàng'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Home;
