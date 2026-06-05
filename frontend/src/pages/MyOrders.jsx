/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import EmptyState from '../components/EmptyState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import Notice from '../components/Notice';
import { formatMoney, getApiError, orderStatusLabel, paymentMethodLabel, statusClass } from '../utils/format';

const sortOrdersNewestFirst = (items) =>
  [...items].sort((a, b) => {
    const createdDiff = new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime();
    return createdDiff || Number(b.id || 0) - Number(a.id || 0);
  });

const orderTabs = [
  { value: 'ALL', label: 'Tất cả' },
  { value: 'PENDING', label: 'Chờ xác nhận' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'SHIPPING', label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

const MyOrders = () => {
  const [orders, setOrders] = useState([]);
  const [activeStatus, setActiveStatus] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState({ type: '', text: '' });
  const [reviewForms, setReviewForms] = useState({});

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const response = await api.get('/orders/my-orders');
      setOrders(sortOrdersNewestFirst(response.data || []));
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể tải đơn hàng.') });
    } finally {
      setLoading(false);
    }
  };

  const statusCounts = orders.reduce(
    (counts, order) => ({
      ...counts,
      [order.status]: (counts[order.status] || 0) + 1,
      ALL: counts.ALL + 1,
    }),
    { ALL: 0 }
  );
  const visibleOrders = activeStatus === 'ALL' ? orders : orders.filter((order) => order.status === activeStatus);

  useEffect(() => {
    fetchOrders();
  }, []);

  const cancelOrder = async (orderId) => {
    if (!window.confirm('Bạn muốn hủy đơn hàng này?')) return;
    try {
      await api.patch(`/orders/${orderId}/cancel`);
      setNotice({ type: 'success', text: 'Đã hủy đơn hàng.' });
      fetchOrders();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể hủy đơn hàng.') });
    }
  };

  const submitReview = async (item) => {
    const form = reviewForms[item.id] || { rating: 5, comment: '' };
    try {
      const productId = item.productId || item.skuProductId;
      if (!productId) {
        setNotice({ type: 'error', text: 'Thiếu mã sản phẩm trong dữ liệu đơn hàng. Vui lòng kiểm tra backend OrderItemResponse.' });
        return;
      }
      await api.post(`/products/${productId}/reviews`, {
        orderItemId: item.id,
        rating: Number(form.rating),
        comment: form.comment,
      });
      setNotice({ type: 'success', text: 'Đã gửi đánh giá sản phẩm.' });
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể gửi đánh giá.') });
    }
  };

  return (
    <main className="mx-auto max-w-5xl px-4 py-6 md:px-6">
      <div className="mb-5">
        <h1 className="text-2xl font-bold text-gray-950">Đơn mua của tôi</h1>
        <p className="mt-1 text-sm text-gray-500">Theo dõi trạng thái, hủy đơn khi còn chờ xác nhận và đánh giá sau khi nhận hàng.</p>
      </div>

      <Notice type={notice.type} message={notice.text} />

      {loading ? (
        <LoadingSkeleton rows={5} />
      ) : orders.length === 0 ? (
        <EmptyState title="Chưa có đơn hàng" description="Các đơn đã đặt sẽ hiển thị tại đây." />
      ) : (
        <div className="mt-4 space-y-4">
          <div className="flex gap-2 overflow-x-auto border-b border-gray-200 pb-2">
            {orderTabs.map((tab) => (
              <button
                key={tab.value}
                type="button"
                onClick={() => setActiveStatus(tab.value)}
                className={`flex-none rounded-md px-3 py-2 text-sm font-semibold ${
                  activeStatus === tab.value ? 'bg-gray-950 text-white' : 'bg-white text-gray-600 hover:bg-gray-100'
                }`}
              >
                {tab.label} ({statusCounts[tab.value] || 0})
              </button>
            ))}
          </div>

          {visibleOrders.length === 0 ? (
            <EmptyState title="Không có đơn trong trạng thái này" description="Chọn trạng thái khác để xem đơn hàng." />
          ) : visibleOrders.map((order) => (
            <section key={order.id} className="rounded-md border border-gray-200 bg-white p-5">
              <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                <div>
                  <div className="flex items-center gap-3">
                    <h2 className="font-bold text-gray-950">Đơn hàng #{order.id}</h2>
                    <span className={`rounded-full border px-3 py-1 text-xs font-semibold ${statusClass[order.status] || 'border-gray-200 bg-gray-50 text-gray-600'}`}>
                      {orderStatusLabel[order.status] || order.status}
                    </span>
                  </div>
                  <p className="mt-1 text-sm text-gray-500">{paymentMethodLabel[order.paymentMethod] || order.paymentMethod}</p>
                </div>
                <div className="text-left md:text-right">
                  <div className="text-sm text-gray-500">Tổng tiền</div>
                  <div className="text-xl font-bold text-red-600">{formatMoney(order.totalAmount)}</div>
                </div>
              </div>

              <div className="mt-4 rounded-md bg-gray-50 p-3 text-sm text-gray-600">
                <div className="font-semibold text-gray-950">{order.recipientName || 'Chưa có tên người nhận'}</div>
                <div>{order.shippingAddress}</div>
                <div>{order.phoneNumber}</div>
              </div>

              <div className="mt-4 space-y-3">
                {order.items?.map((item) => {
                  const form = reviewForms[item.id] || { rating: 5, comment: '' };
                  return (
                    <div key={item.id} className="rounded-md border border-gray-100 p-3">
                      <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                        <Link to={`/products/${item.productId}`} className="flex min-w-0 gap-3 hover:text-primary">
                          <img src={item.imageUrl || 'https://via.placeholder.com/80'} alt={item.productName} className="h-16 w-16 flex-none rounded-md object-cover" />
                          <div className="min-w-0">
                            <div className="font-semibold text-gray-950">{item.productName}</div>
                            <div className="text-xs text-gray-500">{item.tierIndex || 'Mặc định'} · x{item.quantity}</div>
                          </div>
                        </Link>
                        <div className="font-semibold">{formatMoney(item.subtotal)}</div>
                      </div>

                      {order.status === 'DELIVERED' && (
                        <div className="mt-3 grid gap-2 md:grid-cols-[110px_1fr_auto]">
                          <select value={form.rating} onChange={(e) => setReviewForms({ ...reviewForms, [item.id]: { ...form, rating: e.target.value } })} className="rounded-md border border-gray-300 px-3 py-2 text-sm">
                            {[5, 4, 3, 2, 1].map((rating) => <option key={rating} value={rating}>{rating} sao</option>)}
                          </select>
                          <input value={form.comment} onChange={(e) => setReviewForms({ ...reviewForms, [item.id]: { ...form, comment: e.target.value } })} placeholder="Nhận xét sản phẩm" className="rounded-md border border-gray-300 px-3 py-2 text-sm" />
                          <button onClick={() => submitReview(item)} className="rounded-md bg-primary px-4 py-2 text-sm font-semibold text-white">Gửi đánh giá</button>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              {order.status === 'PENDING' && (
                <div className="mt-4 flex justify-end">
                  <button onClick={() => cancelOrder(order.id)} className="rounded-md border border-red-200 px-4 py-2 text-sm font-semibold text-red-600">
                    Hủy đơn
                  </button>
                </div>
              )}
            </section>
          ))}
        </div>
      )}
    </main>
  );
};

export default MyOrders;
