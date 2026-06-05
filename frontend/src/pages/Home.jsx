/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import EmptyState from '../components/EmptyState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import Notice from '../components/Notice';
import { formatMoney, getApiError } from '../utils/format';
import { useAuth } from '../context/AuthContext';

const PAGE_SIZE = 12;

const Home = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({ name: '', categoryId: '', brandId: '', minPrice: '', maxPrice: '', sortBy: 'createdAt', sortDir: 'desc' });
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [now] = useState(() => Date.now());
  const [wishlistIds, setWishlistIds] = useState(new Set());

  const visiblePages = useMemo(() => {
    const count = Math.min(totalPages, 5);
    const start = Math.max(0, Math.min(page - 2, totalPages - count));
    return Array.from({ length: count }, (_, index) => start + index);
  }, [page, totalPages]);

  const fetchReferenceData = async () => {
    try {
      const [categoryRes, brandRes] = await Promise.all([api.get('/categories'), api.get('/brands')]);
      setCategories(categoryRes.data || []);
      setBrands(brandRes.data || []);
    } catch {
      // Bộ lọc phụ không được làm hỏng trang danh sách.
    }
  };

  const fetchProducts = async (targetPage = page, targetSize = PAGE_SIZE, nextFilters = filters) => {
    setLoading(true);
    setError('');
    try {
      const response = await api.get('/products', {
        params: {
          name: nextFilters.name || undefined,
          categoryId: nextFilters.categoryId || undefined,
          brandId: nextFilters.brandId || undefined,
          minPrice: nextFilters.minPrice || undefined,
          maxPrice: nextFilters.maxPrice || undefined,
          page: targetPage,
          size: targetSize,
          sortBy: nextFilters.sortBy,
          sortDir: nextFilters.sortDir,
        },
      });
      setProducts(response.data.content || []);
      setPage(response.data.page || 0);
      setTotalPages(response.data.totalPages || 0);
      setTotalElements(response.data.totalElements || 0);
    } catch (err) {
      setError(getApiError(err, 'Không thể tải danh sách sản phẩm.'));
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReferenceData();
    fetchProducts(0, PAGE_SIZE);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    let active = true;
    if (!user) {
      window.queueMicrotask(() => {
        if (active) setWishlistIds(new Set());
      });
      return () => {
        active = false;
      };
    }

    api.get('/wishlist/ids')
      .then((response) => {
        if (active) setWishlistIds(new Set(response.data || []));
      })
      .catch(() => {
        if (active) setWishlistIds(new Set());
      });

    return () => {
      active = false;
    };
  }, [user]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      fetchProducts(0, PAGE_SIZE, filters);
    }, 350);
    return () => window.clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.name, filters.categoryId, filters.brandId, filters.minPrice, filters.maxPrice, filters.sortBy, filters.sortDir]);

  const updateSort = (value) => {
    const [sortBy, sortDir] = value.split(':');
    const next = { ...filters, sortBy, sortDir };
    setFilters(next);
    fetchProducts(0, PAGE_SIZE, next);
  };

  const addToCart = async (event, skuId) => {
    event.preventDefault();
    if (!user) {
      navigate('/login');
      return;
    }
    try {
      const response = await api.post('/cart', { skuId, quantity: 1 });
      window.dispatchEvent(new CustomEvent('cart:updated', { detail: response.data }));
      alert('Đã thêm sản phẩm vào giỏ hàng.');
    } catch (err) {
      alert(getApiError(err, 'Không thể thêm vào giỏ hàng.'));
    }
  };

  const toggleWishlist = async (event, productId) => {
    event.preventDefault();
    event.stopPropagation();
    if (!user) {
      navigate('/login');
      return;
    }

    try {
      const response = wishlistIds.has(productId)
        ? await api.delete(`/wishlist/${productId}`)
        : await api.post(`/wishlist/${productId}`);
      setWishlistIds(new Set(response.data || []));
    } catch (err) {
      alert(getApiError(err, 'Không thể cập nhật danh sách yêu thích.'));
    }
  };

  return (
    <main className="mx-auto max-w-7xl px-4 py-6 md:px-6">
      <section className="mb-4 flex flex-col justify-between gap-3 md:flex-row md:items-end">
        <div>
          <h1 className="text-2xl font-bold text-gray-950">Sản phẩm điện máy</h1>
          <p className="mt-1 text-sm text-gray-500">Hiện có {totalElements.toLocaleString('vi-VN')} sản phẩm phù hợp.</p>
        </div>
      </section>

      <section className="mb-5 rounded-md border border-gray-200 bg-white p-3">
        <div className="grid gap-2 md:grid-cols-[1.5fr_1fr_1fr_0.85fr_0.85fr_0.9fr]">
          <input className="rounded-md border border-gray-300 px-3 py-2 text-sm" placeholder="Tìm tên sản phẩm" value={filters.name} onChange={(e) => setFilters({ ...filters, name: e.target.value })} />
          <select className="rounded-md border border-gray-300 px-3 py-2 text-sm" value={filters.categoryId} onChange={(e) => setFilters({ ...filters, categoryId: e.target.value })}>
            <option value="">Tất cả danh mục</option>
            {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
          </select>
          <select className="rounded-md border border-gray-300 px-3 py-2 text-sm" value={filters.brandId} onChange={(e) => setFilters({ ...filters, brandId: e.target.value })}>
            <option value="">Tất cả thương hiệu</option>
            {brands.map((brand) => <option key={brand.id} value={brand.id}>{brand.name}</option>)}
          </select>
          <input className="rounded-md border border-gray-300 px-3 py-2 text-sm" type="number" min="0" placeholder="Giá từ" value={filters.minPrice} onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })} />
          <input className="rounded-md border border-gray-300 px-3 py-2 text-sm" type="number" min="0" placeholder="Giá đến" value={filters.maxPrice} onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })} />
          <select value={`${filters.sortBy}:${filters.sortDir}`} onChange={(e) => updateSort(e.target.value)} className="rounded-md border border-gray-300 px-3 py-2 text-sm">
            <option value="createdAt:desc">Mới nhất</option>
            <option value="name:asc">Tên A-Z</option>
            <option value="name:desc">Tên Z-A</option>
          </select>
        </div>
      </section>

      {error && <div className="mb-4"><Notice type="error" message={error} /></div>}

      {loading ? (
        <LoadingSkeleton rows={6} />
      ) : products.length === 0 ? (
        <EmptyState title="Không tìm thấy sản phẩm" description="Thử đổi từ khóa, danh mục hoặc khoảng giá." />
      ) : (
        <>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
            {products.map((product) => {
              const sku = product.skus?.[0];
              const inStock = Number(sku?.stock || 0) > 0;
              const rating = Number(product.averageRating || 0);
              const isNew = product.createdAt ? now - new Date(product.createdAt).getTime() < 1000 * 60 * 60 * 24 * 14 : true;
              const saved = wishlistIds.has(product.id);
              return (
                <Link key={product.id} to={`/products/${product.id}`} className="group overflow-hidden rounded-md border border-gray-200 bg-white transition-shadow hover:shadow-md">
                  <div className="relative aspect-[4/3] bg-gray-100">
                    <img src={product.imageUrls?.[0] || 'https://via.placeholder.com/500'} alt={product.name} className="h-full w-full object-cover" />
                    <div className="absolute left-2 top-2 flex gap-1">
                      {isNew && <span className="rounded bg-emerald-600 px-2 py-0.5 text-[10px] font-bold text-white">Mới</span>}
                      {rating >= 4.5 && <span className="rounded bg-red-600 px-2 py-0.5 text-[10px] font-bold text-white">Hot</span>}
                    </div>
                    <button onClick={(event) => toggleWishlist(event, product.id)} className={`absolute right-2 top-2 flex h-8 w-8 items-center justify-center rounded-full bg-white/90 text-lg font-bold shadow-sm ${saved ? 'text-red-600' : 'text-gray-700 hover:text-red-600'}`} aria-label="Yêu thích">
                      {saved ? '♥' : '♡'}
                    </button>
                  </div>
                  <div className="p-3">
                    <div className="text-xs text-gray-500">{product.brandName || product.categoryName || 'Sản phẩm'}</div>
                    <h2 className="mt-1 line-clamp-2 min-h-10 text-sm font-semibold text-gray-950 group-hover:text-primary">{product.name}</h2>
                    <div className="mt-2 flex items-center gap-1 text-xs">
                      <span className="font-bold text-amber-500">★</span>
                      <span className="font-semibold text-gray-700">{rating.toFixed(1)}</span>
                      <span className="text-gray-400">({product.reviewCount || 0})</span>
                    </div>
                    <div className="mt-2 flex items-center justify-between gap-2">
                      <span className="font-bold text-red-600">{formatMoney(sku?.price)}</span>
                      <span className={`text-xs font-semibold ${inStock ? 'text-emerald-600' : 'text-gray-400'}`}>{inStock ? 'Còn hàng' : 'Hết hàng'}</span>
                    </div>
                    <button disabled={!sku || !inStock} onClick={(event) => addToCart(event, sku?.id)} className="mt-3 w-full rounded-md bg-gray-950 px-3 py-2 text-sm font-semibold text-white hover:bg-primary disabled:cursor-not-allowed disabled:bg-gray-300">
                      Thêm vào giỏ
                    </button>
                  </div>
                </Link>
              );
            })}
          </div>

          {products.length < 4 && (
            <section className="mt-6 rounded-md border border-dashed border-gray-300 bg-white p-5">
              <h2 className="font-semibold text-gray-950">Gợi ý cho bạn</h2>
              <p className="mt-1 text-sm text-gray-500">Khi có thêm sản phẩm hoặc mở rộng bộ lọc, khu vực này sẽ hiển thị thêm lựa chọn phù hợp.</p>
              <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
                {Array.from({ length: 4 }).map((_, index) => (
                  <div key={index} className="rounded-md bg-gray-50 p-3">
                    <div className="aspect-[4/3] rounded-md bg-gray-200" />
                    <div className="mt-3 h-3 rounded bg-gray-200" />
                    <div className="mt-2 h-3 w-2/3 rounded bg-gray-100" />
                  </div>
                ))}
              </div>
            </section>
          )}

          <div className="mt-6 flex justify-center gap-2">
            <button disabled={page === 0} onClick={() => fetchProducts(page - 1, PAGE_SIZE)} className="rounded-md border border-gray-300 px-3 py-2 text-sm disabled:opacity-40">Trước</button>
            {visiblePages.map((pageIndex) => (
              <button key={pageIndex} onClick={() => fetchProducts(pageIndex, PAGE_SIZE)} className={`rounded-md border px-3 py-2 text-sm ${pageIndex === page ? 'border-primary bg-primary text-white' : 'border-gray-300 bg-white'}`}>
                {pageIndex + 1}
              </button>
            ))}
            <button disabled={page >= totalPages - 1} onClick={() => fetchProducts(page + 1, PAGE_SIZE)} className="rounded-md border border-gray-300 px-3 py-2 text-sm disabled:opacity-40">Sau</button>
          </div>
        </>
      )}
    </main>
  );
};

export default Home;
