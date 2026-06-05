import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import AdminLayout from './AdminLayout';
import Notice from '../components/Notice';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { formatMoney, getApiError, orderStatusLabel } from '../utils/format';

const statusStyle = {
  PENDING: 'bg-amber-50 text-amber-700 border-amber-200',
  CONFIRMED: 'bg-blue-50 text-blue-700 border-blue-200',
  SHIPPING: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  DELIVERED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  CANCELLED: 'bg-red-50 text-red-700 border-red-200',
};

const statusOrder = ['PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED'];

const AdminDashboard = () => {
  const [summary, setSummary] = useState(null);
  const [revenue, setRevenue] = useState([]);
  const [topProducts, setTopProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const [summaryRes, revenueRes, topRes] = await Promise.all([
          api.get('/admin/dashboard/summary'),
          api.get('/admin/dashboard/revenue'),
          api.get('/admin/dashboard/top-products', { params: { limit: 5 } }),
        ]);
        if (!active) return;
        setSummary(summaryRes.data);
        setRevenue(revenueRes.data || []);
        setTopProducts(topRes.data || []);
      } catch (err) {
        if (active) setError(getApiError(err, 'Không thể tải dashboard.'));
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, []);

  const maxRevenue = Math.max(...revenue.map((point) => Number(point.revenue || 0)), 0);
  const pendingOrders = Number(summary?.ordersByStatus?.PENDING || 0);
  const deliveredOrders = Number(summary?.ordersByStatus?.DELIVERED || 0);
  const totalOrders = Number(summary?.totalOrders || 0);
  const deliveredRate = totalOrders ? Math.round((deliveredOrders / totalOrders) * 100) : 0;

  return (
    <AdminLayout title="Tổng quan" description="Theo dõi doanh thu, trạng thái đơn và sản phẩm bán chạy.">
      {error && <Notice type="error" message={error} />}
      {loading ? (
        <LoadingSkeleton rows={5} />
      ) : (
        <div className="space-y-5">
          <div className="grid gap-3 md:grid-cols-3">
            <div className="rounded-md border border-gray-200 bg-white p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <div className="text-sm font-medium text-gray-500">Doanh thu đã giao</div>
                <span className="rounded-full bg-emerald-50 px-2 py-1 text-xs font-bold text-emerald-700">Ổn định</span>
              </div>
              <div className="mt-2 text-2xl font-bold text-gray-950">{formatMoney(summary?.deliveredRevenue)}</div>
              <div className="mt-3 text-xs text-gray-500">Tính theo đơn đã giao thành công.</div>
            </div>
            <div className="rounded-md border border-gray-200 bg-white p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <div className="text-sm font-medium text-gray-500">Tổng đơn hàng</div>
                <span className="rounded-full bg-amber-50 px-2 py-1 text-xs font-bold text-amber-700">{pendingOrders} chờ xử lý</span>
              </div>
              <div className="mt-2 text-2xl font-bold text-gray-950">{totalOrders}</div>
              <div className="mt-3 text-xs text-gray-500">{deliveredRate}% đơn đã hoàn tất.</div>
            </div>
            <div className="rounded-md border border-gray-200 bg-white p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <div className="text-sm font-medium text-gray-500">Sản phẩm bán chạy</div>
                <span className="rounded-full bg-blue-50 px-2 py-1 text-xs font-bold text-blue-700">Top 1</span>
              </div>
              <div className="mt-2 line-clamp-1 text-2xl font-bold text-gray-950">{topProducts[0]?.productName || 'Đang chờ dữ liệu'}</div>
              <div className="mt-3 text-xs text-gray-500">{topProducts[0] ? `${topProducts[0].quantitySold} sản phẩm đã bán.` : 'Sẽ tự cập nhật khi có đơn đã giao.'}</div>
            </div>
          </div>

          <div className="flex flex-wrap gap-3 rounded-md border border-gray-200 bg-white p-4">
            <Link to="/admin/products/new" className="rounded-md bg-primary px-4 py-2 text-sm font-semibold text-white hover:bg-primary-dark">Thêm sản phẩm</Link>
            <Link to="/admin/orders" className="rounded-md border border-amber-200 bg-amber-50 px-4 py-2 text-sm font-semibold text-amber-700 hover:bg-amber-100">Xử lý đơn</Link>
            <Link to="/admin/products" className="rounded-md border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50">Quản lý tồn kho</Link>
          </div>

          <div className="grid gap-5 lg:grid-cols-[1fr_1fr]">
            <div className="rounded-md border border-gray-200 bg-white p-5 shadow-sm">
              <h2 className="font-semibold text-gray-950">Đơn hàng theo trạng thái</h2>
              <div className="mt-4 space-y-3">
                {statusOrder.map((status) => {
                  const count = Number(summary?.ordersByStatus?.[status] || 0);
                  return (
                  <Link key={status} to="/admin/orders" className={`flex items-center justify-between rounded-md border px-3 py-2 text-sm ${statusStyle[status]}`}>
                    <span>{orderStatusLabel[status] || status}</span>
                    <span className="font-bold">{count}</span>
                  </Link>
                );
                })}
              </div>
            </div>

            <div className="rounded-md border border-gray-200 bg-white p-5 shadow-sm">
              <h2 className="font-semibold text-gray-950">Top sản phẩm bán chạy</h2>
              <div className="mt-4 space-y-3">
                {topProducts.length === 0 ? (
                  <div className="rounded-md border border-dashed border-gray-300 bg-gray-50 p-4 text-sm text-gray-500">
                    Chưa có sản phẩm bán chạy. Khi đơn hàng được giao, top sản phẩm sẽ hiển thị tại đây.
                  </div>
                ) : (
                  topProducts.map((item, index) => (
                    <div key={`${item.productId}-${item.productName}`} className="flex items-center justify-between gap-3 rounded-md bg-gray-50 px-3 py-2 text-sm">
                      <span className="line-clamp-1"><span className="mr-2 font-bold text-primary">#{index + 1}</span>{item.productName}</span>
                      <span className="shrink-0 font-bold">{item.quantitySold} bán</span>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>

          <div className="rounded-md border border-gray-200 bg-white p-5 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold text-gray-950">Doanh thu theo ngày</h2>
              <span className="text-xs font-semibold text-gray-500">Đơn đã giao</span>
            </div>
            <div className="mt-4 h-56 rounded-md border border-gray-100 bg-gray-50 p-4">
              {revenue.length === 0 ? (
                <div className="flex h-full flex-col items-center justify-center text-center">
                  <div className="text-sm font-semibold text-gray-700">Chưa có doanh thu</div>
                  <p className="mt-1 max-w-sm text-sm text-gray-500">Tạo đơn test và chuyển sang trạng thái đã giao để biểu đồ bắt đầu có dữ liệu.</p>
                </div>
              ) : (
                <div className="flex h-full items-end gap-3">
                  {revenue.map((point) => {
                    const value = Number(point.revenue || 0);
                    const height = maxRevenue ? Math.max(10, Math.round((value / maxRevenue) * 100)) : 10;
                    return (
                      <div key={point.date} className="flex min-w-16 flex-1 flex-col items-center gap-2">
                        <div className="text-xs font-semibold text-gray-700">{formatMoney(value)}</div>
                        <div className="w-full rounded-t-md bg-primary" style={{ height: `${height}%` }} />
                        <div className="text-xs text-gray-500">{point.date}</div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </AdminLayout>
  );
};

export default AdminDashboard;
