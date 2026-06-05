/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api from '../api/axios';
import EmptyState from '../components/EmptyState';
import Notice from '../components/Notice';
import { formatMoney, getApiError } from '../utils/format';
import { useAuth } from '../context/AuthContext';

const ProductDetail = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [selectedSku, setSelectedSku] = useState(null);
  const [selectedImageUrl, setSelectedImageUrl] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchProduct = async () => {
    setLoading(true);
    setError('');
    try {
      const [productRes, reviewRes] = await Promise.all([
        api.get(`/products/${id}`),
        api.get(`/products/${id}/reviews`),
      ]);
      setProduct(productRes.data);
      setSelectedSku(productRes.data.skus?.[0] || null);
      setSelectedImageUrl(productRes.data.imageUrls?.[0] || '');
      setReviews(reviewRes.data || []);
    } catch (err) {
      setError(getApiError(err, 'Không thể tải chi tiết sản phẩm.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProduct();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const addToCart = async () => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (!selectedSku) return;
    try {
      await api.post('/cart', { skuId: selectedSku.id, quantity });
      alert('Đã thêm sản phẩm vào giỏ hàng.');
    } catch (err) {
      alert(getApiError(err, 'Không thể thêm vào giỏ hàng.'));
    }
  };

  const buyNow = async () => {
    await addToCart();
    if (user) navigate('/checkout');
  };

  if (loading) return <main className="mx-auto max-w-7xl px-4 py-10 text-sm text-gray-500">Đang tải chi tiết sản phẩm...</main>;

  if (error) return <main className="mx-auto max-w-7xl px-4 py-10"><Notice type="error" message={error} /></main>;

  if (!product) {
    return (
      <main className="mx-auto max-w-7xl px-4 py-10">
        <EmptyState title="Không tìm thấy sản phẩm" action={<Link to="/" className="rounded-md bg-primary px-4 py-2 text-sm font-semibold text-white">Quay lại danh sách</Link>} />
      </main>
    );
  }

  const inStock = Number(selectedSku?.stock || 0) > 0;
  const imageUrls = product.imageUrls?.length ? product.imageUrls : ['https://via.placeholder.com/700'];
  const activeImageUrl = selectedImageUrl || imageUrls[0];

  return (
    <main className="mx-auto max-w-7xl px-4 py-6 md:px-6">
      <div className="grid gap-8 lg:grid-cols-[1fr_1fr]">
        <section>
          <div className="overflow-hidden rounded-md border border-gray-200 bg-white">
            <img src={activeImageUrl} alt={product.name} className="aspect-square w-full object-cover" />
          </div>
          {imageUrls.length > 1 && (
            <div className="mt-3 grid grid-cols-4 gap-3 sm:grid-cols-5">
              {imageUrls.slice(0, 10).map((url, index) => (
                <button
                  key={`${url}-${index}`}
                  type="button"
                  onClick={() => setSelectedImageUrl(url)}
                  className={`overflow-hidden rounded-md border bg-white p-1 ${activeImageUrl === url ? 'border-primary ring-2 ring-primary/20' : 'border-gray-200 hover:border-gray-400'}`}
                >
                  <img src={url} alt={`${product.name} ${index + 1}`} className="aspect-square w-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </section>

        <section className="rounded-md border border-gray-200 bg-white p-5">
          <div className="text-sm text-gray-500">{product.brandName || 'Thương hiệu'} · {product.categoryName || 'Danh mục'}</div>
          <h1 className="mt-2 text-3xl font-bold text-gray-950">{product.name}</h1>
          <div className="mt-3 text-sm text-gray-500">
            Đánh giá {Number(product.averageRating || 0).toFixed(1)}/5 · {product.reviewCount || 0} lượt đánh giá
          </div>
          <div className="mt-5 text-3xl font-bold text-red-600">{formatMoney(selectedSku?.price)}</div>
          <p className="mt-5 text-sm leading-6 text-gray-600">{product.description || 'Chưa có mô tả sản phẩm.'}</p>

          <div className="mt-6">
            <div className="text-sm font-semibold text-gray-950">Phân loại</div>
            <div className="mt-2 flex flex-wrap gap-2">
              {product.skus?.map((sku) => (
                <button
                  key={sku.id}
                  onClick={() => setSelectedSku(sku)}
                  className={`rounded-md border px-3 py-2 text-sm font-semibold ${selectedSku?.id === sku.id ? 'border-primary bg-primary text-white' : 'border-gray-300 bg-white text-gray-700'}`}
                >
                  {sku.tierIndex || 'Mặc định'}
                </button>
              ))}
            </div>
          </div>

          <div className="mt-5 grid grid-cols-[140px_1fr] gap-4">
            <div>
              <label className="text-sm font-semibold text-gray-950">Số lượng</label>
              <input
                type="number"
                min="1"
                max={selectedSku?.stock || 1}
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
                className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              />
            </div>
            <div>
              <div className="text-sm font-semibold text-gray-950">Tồn kho</div>
              <div className="mt-2 rounded-md bg-gray-50 px-3 py-2 text-sm text-gray-600">{selectedSku?.stock || 0} sản phẩm</div>
            </div>
          </div>

          <div className="mt-6 flex flex-col gap-3 sm:flex-row">
            <button disabled={!inStock} onClick={addToCart} className="flex-1 rounded-md bg-gray-950 px-4 py-3 text-sm font-semibold text-white hover:bg-primary disabled:bg-gray-300">
              Thêm vào giỏ
            </button>
            <button disabled={!inStock} onClick={buyNow} className="flex-1 rounded-md border border-gray-950 px-4 py-3 text-sm font-semibold text-gray-950 hover:bg-gray-50 disabled:border-gray-300 disabled:text-gray-400">
              Mua ngay
            </button>
          </div>
        </section>
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_1fr]">
        <section className="rounded-md border border-gray-200 bg-white p-5">
          <h2 className="font-bold text-gray-950">Thông số kỹ thuật</h2>
          {product.specs?.length ? (
            <dl className="mt-4 divide-y divide-gray-100">
              {product.specs.map((spec, index) => (
                <div key={`${spec.key}-${index}`} className="grid grid-cols-[160px_1fr] gap-4 py-3 text-sm">
                  <dt className="font-medium text-gray-500">{spec.key}</dt>
                  <dd className="text-gray-950">{spec.value}</dd>
                </div>
              ))}
            </dl>
          ) : (
            <p className="mt-3 text-sm text-gray-500">Chưa có thông số kỹ thuật.</p>
          )}
        </section>

        <section className="rounded-md border border-gray-200 bg-white p-5">
          <h2 className="font-bold text-gray-950">Đánh giá sản phẩm</h2>
          {reviews.length === 0 ? (
            <p className="mt-3 text-sm text-gray-500">Chưa có đánh giá.</p>
          ) : (
            <div className="mt-4 space-y-3">
              {reviews.map((review) => (
                <div key={review.id} className="rounded-md bg-gray-50 p-3 text-sm">
                  <div className="flex justify-between gap-3">
                    <span className="font-semibold text-gray-950">{review.customerName || `Khách hàng #${review.customerId}`}</span>
                    <span className="font-semibold text-amber-600">{review.rating}/5</span>
                  </div>
                  {review.comment && <p className="mt-2 text-gray-600">{review.comment}</p>}
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </main>
  );
};

export default ProductDetail;
