/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import AdminLayout from './AdminLayout';
import Notice from '../components/Notice';
import EmptyState from '../components/EmptyState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { formatMoney, getApiError, orderStatusLabel, paymentMethodLabel, paymentStatusLabel, statusClass } from '../utils/format';

const sortOrdersNewestFirst = (items) =>
  [...items].sort((a, b) => {
    const createdDiff = new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime();
    return createdDiff || Number(b.id || 0) - Number(a.id || 0);
  });

const nextStatusOptions = {
  PENDING_PAYMENT: ['CANCELLED'],
  PENDING: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['SHIPPING'],
  SHIPPING: ['DELIVERED'],
};

const orderTabs = [
  { value: 'ALL', label: 'Tất cả' },
  { value: 'PENDING_PAYMENT', label: 'Chờ thanh toán' },
  { value: 'PENDING', label: 'Chờ xác nhận' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'SHIPPING', label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

const AdminOrders = () => {
  const [orders, setOrders] = useState([]);
  const [activeStatus, setActiveStatus] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState({ type: '', text: '' });
  const [refundRefs, setRefundRefs] = useState({});

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const response = await api.get('/orders');
      setOrders(sortOrdersNewestFirst(response.data || []));
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể tải đơn hàng.') });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const updateStatus = async (orderId, status) => {
    try {
      await api.patch(`/orders/${orderId}/status`, null, { params: { status } });
      setNotice({ type: 'success', text: 'Đã cập nhật trạng thái đơn hàng.' });
      fetchOrders();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể cập nhật trạng thái.') });
    }
  };

  const markRefunded = async (orderId) => {
    const providerRef = refundRefs[orderId] || '';
    try {
      await api.patch(`/orders/${orderId}/payment/refunded`, null, { params: { providerRef: providerRef || undefined } });
      setNotice({ type: 'success', text: 'Đã xác nhận hoàn tiền.' });
      setRefundRefs({ ...refundRefs, [orderId]: '' });
      fetchOrders();
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể xác nhận hoàn tiền.') });
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

  return (
    <AdminLayout title="Đơn hàng" description="Theo dõi và xử lý trạng thái đơn hàng.">
      <div className="space-y-4">
        <Notice type={notice.type} message={notice.text} />
        {loading ? (
          <LoadingSkeleton rows={5} />
        ) : orders.length === 0 ? (
          <EmptyState title="Chưa có đơn hàng" description="Đơn hàng mới sẽ xuất hiện tại đây." />
        ) : (
          <div className="space-y-4">
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
              <div key={order.id} className="rounded-md border border-gray-200 bg-white p-5">
                <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                  <div>
                    <div className="flex items-center gap-3">
                      <h2 className="font-bold text-gray-950">Đơn hàng #{order.id}</h2>
                      <span className={`rounded-full border px-3 py-1 text-xs font-semibold ${statusClass[order.status] || 'border-gray-200 bg-gray-50 text-gray-600'}`}>
                        {orderStatusLabel[order.status] || order.status}
                      </span>
                    </div>
                    <p className="mt-1 text-sm text-gray-500">
                      Khách hàng #{order.userId} · {paymentMethodLabel[order.paymentMethod] || order.paymentMethod}
                      {order.paymentStatus ? ` · ${paymentStatusLabel[order.paymentStatus] || order.paymentStatus}` : ''}
                    </p>
                  </div>
                  <div className="text-left md:text-right">
                    <div className="text-sm text-gray-500">Tổng tiền</div>
                    <div className="text-xl font-bold text-gray-950">{formatMoney(order.totalAmount)}</div>
                  </div>
                </div>

                <div className="mt-4 grid gap-4 md:grid-cols-[1fr_260px]">
                  <div className="space-y-3">
                    {order.items?.map((item) => (
                      <div key={item.id} className="flex justify-between gap-3 rounded-md bg-gray-50 px-3 py-2 text-sm">
                        <Link to={`/admin/products/${item.productId}/edit`} className="flex min-w-0 gap-3 hover:text-primary">
                          <img src={item.imageUrl || 'https://via.placeholder.com/72'} alt={item.productName} className="h-14 w-14 flex-none rounded-md object-cover" />
                          <div className="min-w-0">
                            <div className="font-medium text-gray-950">{item.productName}</div>
                            <div className="text-xs text-gray-500">{item.tierIndex || 'Mặc định'} · x{item.quantity}</div>
                          </div>
                        </Link>
                        <div className="font-semibold">{formatMoney(item.subtotal)}</div>
                      </div>
                    ))}
                  </div>

                  <div className="rounded-md bg-gray-50 p-3 text-sm">
                    <div className="font-semibold text-gray-950">Thông tin nhận hàng</div>
                    <div className="mt-2 font-medium text-gray-700">{order.recipientName || 'Chưa có tên người nhận'}</div>
                    <div className="mt-1 text-gray-600">{order.shippingAddress}</div>
                    <div className="mt-1 text-gray-600">{order.phoneNumber}</div>
                    <div className="mt-4">
                      {order.paymentStatus === 'REFUND_PENDING' && (
                        <div className="mb-3 space-y-2 rounded-md border border-amber-200 bg-amber-50 p-3">
                          <input
                            value={refundRefs[order.id] || ''}
                            onChange={(e) => setRefundRefs({ ...refundRefs, [order.id]: e.target.value })}
                            placeholder="Mã tham chiếu hoàn tiền"
                            className="w-full rounded-md border border-amber-200 bg-white px-3 py-2 text-sm"
                          />
                          <button
                            type="button"
                            onClick={() => markRefunded(order.id)}
                            className="w-full rounded-md bg-amber-600 px-3 py-2 text-sm font-semibold text-white"
                          >
                            Xác nhận đã hoàn tiền
                          </button>
                        </div>
                      )}
                      {nextStatusOptions[order.status]?.length ? (
                        <select
                          value=""
                          onChange={(e) => e.target.value && updateStatus(order.id, e.target.value)}
                          className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm"
                        >
                          <option value="">Cập nhật trạng thái</option>
                          {nextStatusOptions[order.status].map((status) => (
                            <option key={status} value={status}>{orderStatusLabel[status]}</option>
                          ))}
                        </select>
                      ) : (
                        <div className="text-xs text-gray-500">Không còn trạng thái tiếp theo.</div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminOrders;
