import React, { useEffect, useState } from 'react';
import api from '../api/axios';

const ShopOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchOrders = async () => {
    try {
      const response = await api.get('/orders/shop-orders');
      setOrders(response.data);
    } catch (err) {
      console.error('Failed to fetch shop orders', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    (async () => {
      await fetchOrders();
    })();
  }, []);

  const updateStatus = async (orderId, newStatus) => {
    try {
      await api.patch(`/orders/${orderId}/status?status=${newStatus}`);
      alert('Cập nhật trạng thái thành công!');
      fetchOrders();
    } catch {
      alert('Lỗi khi cập nhật trạng thái.');
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-500 italic">Đang tải danh sách đơn hàng của Shop...</div>;

  return (
    <div className="max-w-6xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-8 text-gray-800 uppercase">Quản lý Đơn hàng</h1>
      
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead className="bg-gray-50 text-gray-600 text-sm uppercase">
            <tr>
              <th className="p-4 border-b font-bold">Mã Đơn</th>
              <th className="p-4 border-b font-bold">Khách hàng</th>
              <th className="p-4 border-b font-bold">Sản phẩm</th>
              <th className="p-4 border-b font-bold">Tổng tiền</th>
              <th className="p-4 border-b font-bold">Trạng thái</th>
              <th className="p-4 border-b font-bold text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 text-sm">
            {orders.map((order) => (
              <tr key={order.id} className="hover:bg-gray-50 transition">
                <td className="p-4">#{order.id}</td>
                <td className="p-4">
                  <div className="font-bold text-gray-800">{order.userName}</div>
                </td>
                <td className="p-4">
                  {order.items.map(item => (
                    <div key={item.id} className="text-xs text-gray-500">
                      • {item.productName} ({item.tierIndex}) x{item.quantity}
                    </div>
                  ))}
                </td>
                <td className="p-4 font-bold text-primary">₫{order.totalAmount?.toLocaleString()}</td>
                <td className="p-4">
                  <span className={`text-[10px] font-bold px-2 py-1 rounded-full uppercase ${
                    order.status === 'DELIVERED' ? 'bg-green-100 text-green-700' : 
                    order.status === 'PENDING' ? 'bg-orange-100 text-orange-700' :
                    order.status === 'PROCESSING' ? 'bg-blue-100 text-blue-700' :
                    order.status === 'SHIPPED' ? 'bg-indigo-100 text-indigo-700' :
                    'bg-gray-100 text-gray-700'
                  }`}>
                    {order.status}
                  </span>
                </td>
                <td className="p-4">
                  <div className="flex justify-center gap-2">
                    {order.status === 'PENDING' && (
                      <button onClick={() => updateStatus(order.id, 'PROCESSING')} className="bg-blue-600 text-white px-3 py-1 rounded text-xs font-bold hover:bg-blue-700">Xac nhan don</button>
                    )}
                    {order.status === 'PROCESSING' && (
                      <button onClick={() => updateStatus(order.id, 'SHIPPED')} className="bg-indigo-600 text-white px-3 py-1 rounded text-xs font-bold hover:bg-indigo-700">Ban giao cho van chuyen</button>
                    )}
                    {order.status === 'SHIPPED' && (
                      <button onClick={() => updateStatus(order.id, 'DELIVERED')} className="bg-green-600 text-white px-3 py-1 rounded text-xs font-bold hover:bg-green-700">Da giao hang</button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {orders.length === 0 && (
          <div className="p-12 text-center text-gray-400">Shop chưa có đơn hàng nào mới.</div>
        )}
      </div>
    </div>
  );
};

export default ShopOrders;
