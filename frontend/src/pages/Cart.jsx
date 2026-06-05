/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import EmptyState from '../components/EmptyState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import Notice from '../components/Notice';
import { formatMoney, getApiError } from '../utils/format';

const Cart = () => {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState({ type: '', text: '' });

  const fetchCart = async () => {
    setLoading(true);
    try {
      const response = await api.get('/cart');
      setCart(response.data);
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể tải giỏ hàng.') });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const updateQuantity = async (itemId, quantity) => {
    if (quantity < 1) return;
    try {
      await api.patch(`/cart/${itemId}`, { quantity });
      fetchCart();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể cập nhật số lượng.') });
    }
  };

  const removeItem = async (itemId) => {
    try {
      await api.delete(`/cart/${itemId}`);
      setNotice({ type: 'success', text: 'Đã xóa sản phẩm khỏi giỏ hàng.' });
      fetchCart();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể xóa sản phẩm.') });
    }
  };

  return (
    <main className="mx-auto max-w-6xl px-4 py-6 md:px-6">
      <div className="mb-5 flex items-end justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-950">Giỏ hàng</h1>
          <p className="mt-1 text-sm text-gray-500">Kiểm tra sản phẩm trước khi đặt hàng.</p>
        </div>
        <Link to="/" className="text-sm font-semibold text-primary">Tiếp tục mua sắm</Link>
      </div>

      <Notice type={notice.type} message={notice.text} />

      {loading ? (
        <LoadingSkeleton rows={4} />
      ) : !cart?.items?.length ? (
        <EmptyState
          title="Giỏ hàng trống"
          description="Hãy chọn sản phẩm trước khi thanh toán."
          action={<Link to="/" className="rounded-md bg-primary px-4 py-2 text-sm font-semibold text-white">Mua sắm ngay</Link>}
        />
      ) : (
        <div className="mt-4 grid gap-5 lg:grid-cols-[1fr_320px]">
          <section className="space-y-3">
            {cart.items.map((item) => (
              <div key={item.id} className="rounded-md border border-gray-200 bg-white p-4">
                <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                  <div className="flex min-w-0 gap-4">
                    <img src={item.imageUrl || 'https://via.placeholder.com/96'} alt={item.productName} className="h-24 w-24 flex-none rounded-md object-cover" />
                    <div className="min-w-0">
                      <h2 className="font-semibold text-gray-950">{item.productName}</h2>
                      <p className="mt-1 text-sm text-gray-500">Phân loại: {item.tierIndex || 'Mặc định'}</p>
                      <p className="mt-2 font-semibold text-red-600">{formatMoney(item.price)}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <input type="number" min="1" value={item.quantity} onChange={(e) => updateQuantity(item.id, Number(e.target.value))} className="w-20 rounded-md border border-gray-300 px-3 py-2 text-sm" />
                    <div className="w-32 text-right font-bold text-gray-950">{formatMoney(item.subtotal)}</div>
                    <button onClick={() => removeItem(item.id)} className="rounded-md border border-red-200 px-3 py-2 text-sm font-semibold text-red-600">Xóa</button>
                  </div>
                </div>
              </div>
            ))}
          </section>

          <aside className="h-fit rounded-md border border-gray-200 bg-white p-5">
            <h2 className="font-bold text-gray-950">Tóm tắt đơn hàng</h2>
            <div className="mt-4 space-y-3 text-sm">
              <div className="flex justify-between"><span className="text-gray-500">Tạm tính</span><span className="font-semibold">{formatMoney(cart.totalAmount)}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Vận chuyển</span><span className="font-semibold text-emerald-600">Miễn phí</span></div>
              <div className="border-t border-gray-100 pt-3 flex justify-between"><span className="font-semibold">Tổng cộng</span><span className="text-lg font-bold text-red-600">{formatMoney(cart.totalAmount)}</span></div>
            </div>
            <button onClick={() => navigate('/checkout')} className="mt-5 w-full rounded-md bg-primary px-4 py-3 text-sm font-semibold text-white hover:bg-primary-dark">
              Tiến hành thanh toán
            </button>
          </aside>
        </div>
      )}
    </main>
  );
};

export default Cart;
