import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

const MyShop = () => {
  const [shop, setShop] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchShop = async () => {
      try {
        const response = await api.get('/shops/my-shop');
        setShop(response.data);
      } catch {
        setError('Không tìm thấy thông tin gian hàng hoặc bạn chưa đăng ký shop.');
      } finally {
        setLoading(false);
      }
    };
    fetchShop();
  }, []);

  if (loading) return <div className="p-8 text-center">Đang tải thông tin...</div>;

  return (
    <div className="max-w-4xl mx-auto mt-8 px-4">
      {error ? (
        <div className="bg-red-50 p-6 rounded-lg text-red-600 border border-red-200">{error}</div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="bg-gradient-to-r from-primary to-orange-500 h-32 px-8 flex items-end pb-4">
            <h1 className="text-3xl font-bold text-white uppercase">{shop.name}</h1>
          </div>
          <div className="p-8">
            <div className="flex justify-between items-start mb-8">
              <div>
                <p className="text-gray-500 text-sm mb-1">Mô tả gian hàng</p>
                <p className="text-gray-800 text-lg">{shop.description || 'Chưa có mô tả'}</p>
              </div>
              <div className="text-right">
                <p className="text-gray-500 text-sm mb-1">Trạng thái</p>
                <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider border border-green-200">
                  {shop.status}
                </span>
              </div>
            </div>

            <div className="border-t pt-8 mt-4">
              <h2 className="text-xl font-semibold mb-4 text-gray-800">Quản lý Sản phẩm</h2>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <Link to="/my-shop/add-product" className="border-2 border-dashed border-primary rounded-lg h-40 flex items-center justify-center text-primary font-medium hover:bg-orange-50 transition cursor-pointer">
                  + Thêm sản phẩm mới
                </Link>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyShop;
