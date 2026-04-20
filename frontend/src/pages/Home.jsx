import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

const Home = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [searchName, setSearchName] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  const [size, setSize] = useState(20);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const visiblePages = useMemo(() => {
    if (totalPages <= 7) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    const pages = new Set([0, totalPages - 1, page - 1, page, page + 1]);
    return Array.from(pages)
      .filter((p) => p >= 0 && p < totalPages)
      .sort((a, b) => a - b);
  }, [page, totalPages]);

  const fetchCategories = async () => {
    try {
      const response = await api.get('/categories');
      setCategories(response.data || []);
    } catch (err) {
      console.error('Failed to load categories', err);
    }
  };

  const fetchProducts = async (targetPage = page, targetSize = size, overrides = {}) => {
    setLoading(true);
    setError('');

    try {
      const response = await api.get('/products/discovery', {
        params: {
          name: searchName || undefined,
          categoryId: selectedCategory || undefined,
          minPrice: minPrice || undefined,
          maxPrice: maxPrice || undefined,
          page: targetPage,
          size: targetSize,
          sortBy: overrides.sortBy || sortBy,
          sortDir: overrides.sortDir || sortDir,
        },
      });

      setProducts(response.data.content || []);
      setPage(response.data.page || 0);
      setTotalPages(response.data.totalPages || 0);
      setTotalElements(response.data.totalElements || 0);
    } catch (err) {
      console.error('Failed to fetch products', err);
      setError(err.response?.data?.message || 'Khong the tai danh sach san pham.');
      setProducts([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
    fetchProducts(0, size);
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchProducts(0, size);
  };

  const handlePageChange = (nextPage) => {
    if (nextPage < 0 || nextPage >= totalPages || nextPage === page) {
      return;
    }
    fetchProducts(nextPage, size);
  };

  const handlePageSizeChange = (value) => {
    const nextSize = Number(value);
    setSize(nextSize);
    fetchProducts(0, nextSize);
  };

  const handleSortChange = (value) => {
    if (value === 'name_asc') {
      setSortBy('name');
      setSortDir('asc');
      return fetchProducts(0, size, { sortBy: 'name', sortDir: 'asc' });
    }

    if (value === 'name_desc') {
      setSortBy('name');
      setSortDir('desc');
      return fetchProducts(0, size, { sortBy: 'name', sortDir: 'desc' });
    }

    setSortBy('createdAt');
    setSortDir('desc');
    return fetchProducts(0, size, { sortBy: 'createdAt', sortDir: 'desc' });
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
    <div className="max-w-7xl mx-auto p-4 md:p-6 pb-20">
      <div className="bg-white border border-gray-200 rounded-xl p-4 md:p-6 mb-6">
        <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-gray-900">Khám Phá Sản Phẩm</h1>
            <p className="text-sm text-gray-500 mt-1">Tổng cộng {totalElements.toLocaleString()} sản phẩm</p>
          </div>
          <div className="text-xs text-gray-500">Trang {totalPages === 0 ? 0 : page + 1}/{totalPages}</div>
        </div>

        <form onSubmit={handleSearch} className="grid grid-cols-1 md:grid-cols-6 gap-3 mt-4">
          <input
            type="text"
            placeholder="Tìm theo tên sản phẩm"
            value={searchName}
            onChange={(e) => setSearchName(e.target.value)}
            className="md:col-span-2 px-3 py-2 border border-gray-300 rounded-lg outline-none focus:border-blue-500"
          />

          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg outline-none focus:border-blue-500"
          >
            <option value="">Tất cả danh mục</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>

          <input
            type="number"
            min="0"
            placeholder="Giá từ"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg outline-none focus:border-blue-500"
          />

          <input
            type="number"
            min="0"
            placeholder="Giá đến"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg outline-none focus:border-blue-500"
          />

          <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold">
            Tìm kiếm
          </button>
        </form>

        <div className="flex flex-wrap items-center gap-3 mt-3">
          <select onChange={(e) => handleSortChange(e.target.value)} className="px-3 py-2 border border-gray-300 rounded-lg text-sm" defaultValue="newest">
            <option value="newest">Mới nhất</option>
            <option value="name_asc">Tên A-Z</option>
            <option value="name_desc">Tên Z-A</option>
          </select>

          <select value={size} onChange={(e) => handlePageSizeChange(e.target.value)} className="px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value={15}>15 / trang</option>
            <option value={20}>20 / trang</option>
          </select>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-20 text-gray-500">Đang tải sản phẩm...</div>
      ) : error ? (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg p-4">{error}</div>
      ) : products.length === 0 ? (
        <div className="bg-white border border-gray-200 rounded-lg p-10 text-center text-gray-500">
          Không tìm thấy sản phẩm phù hợp bộ lọc.
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {products.map((product) => (
              <Link
                to={`/products/${product.id}`}
                key={product.id}
                className="bg-white border border-gray-200 rounded-xl overflow-hidden hover:shadow-md transition-shadow"
              >
                <div className="aspect-square bg-gray-100">
                  <img
                    src={product.imageUrls?.[0] || 'https://via.placeholder.com/400'}
                    alt={product.name}
                    className="w-full h-full object-cover"
                  />
                </div>

                <div className="p-3">
                  <div className="text-xs text-gray-500 mb-1 truncate">{product.shopName}</div>
                  <h3 className="font-semibold text-gray-900 line-clamp-2 min-h-[48px]">{product.name}</h3>
                  <div className="mt-2 flex items-center justify-between">
                    <div className="text-red-600 font-bold">{Number(product.skus?.[0]?.price || 0).toLocaleString()} VND</div>
                    <span className="text-xs text-green-600 font-semibold">Còn hàng</span>
                  </div>
                  <button
                    onClick={(e) => product.skus?.[0] && handleAddToCart(e, product.skus[0].id)}
                    className="mt-3 w-full px-3 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 text-sm font-semibold"
                  >
                    Thêm vào giỏ
                  </button>
                </div>
              </Link>
            ))}
          </div>

          <div className="mt-6 flex flex-wrap items-center justify-center gap-2">
            <button
              onClick={() => handlePageChange(page - 1)}
              disabled={page === 0}
              className="px-3 py-2 border border-gray-300 rounded-lg disabled:opacity-40"
            >
              Trước
            </button>

            {visiblePages.map((pageIndex) => (
              <button
                key={pageIndex}
                onClick={() => handlePageChange(pageIndex)}
                className={`px-3 py-2 rounded-lg border ${
                  pageIndex === page ? 'bg-blue-600 border-blue-600 text-white' : 'border-gray-300 hover:bg-gray-50'
                }`}
              >
                {pageIndex + 1}
              </button>
            ))}

            <button
              onClick={() => handlePageChange(page + 1)}
              disabled={totalPages === 0 || page >= totalPages - 1}
              className="px-3 py-2 border border-gray-300 rounded-lg disabled:opacity-40"
            >
              Sau
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default Home;

