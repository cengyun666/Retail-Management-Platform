// 预设图片资源配置
export const presetImages = [
  {
    id: 1,
    name: '夏季促销',
    url: 'https://picsum.photos/seed/summer-sale/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/summer-sale/200/100.jpg',
    category: '促销活动'
  },
  {
    id: 2,
    name: '新品上市',
    url: 'https://picsum.photos/seed/new-products/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/new-products/200/100.jpg',
    category: '产品推广'
  },
  {
    id: 3,
    name: '会员专享',
    url: 'https://picsum.photos/seed/member-exclusive/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/member-exclusive/200/100.jpg',
    category: '会员活动'
  },
  {
    id: 4,
    name: '限时秒杀',
    url: 'https://picsum.photos/seed/flash-sale/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/flash-sale/200/100.jpg',
    category: '促销活动'
  },
  {
    id: 5,
    name: '品牌特惠',
    url: 'https://picsum.photos/seed/brand-discount/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/brand-discount/200/100.jpg',
    category: '促销活动'
  },
  {
    id: 6,
    name: '节日特惠',
    url: 'https://picsum.photos/seed/holiday-special/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/holiday-special/200/100.jpg',
    category: '节日活动'
  },
  {
    id: 7,
    name: '新品推荐',
    url: 'https://picsum.photos/seed/featured-products/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/featured-products/200/100.jpg',
    category: '产品推广'
  },
  {
    id: 8,
    name: '热门商品',
    url: 'https://picsum.photos/seed/hot-items/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/hot-items/200/100.jpg',
    category: '产品推广'
  },
  {
    id: 9,
    name: '清仓特卖',
    url: 'https://picsum.photos/seed/clearance-sale/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/clearance-sale/200/100.jpg',
    category: '促销活动'
  },
  {
    id: 10,
    name: '积分兑换',
    url: 'https://picsum.photos/seed/points-exchange/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/points-exchange/200/100.jpg',
    category: '会员活动'
  },
  {
    id: 11,
    name: '新品首发',
    url: 'https://picsum.photos/seed/product-launch/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/product-launch/200/100.jpg',
    category: '产品推广'
  },
  {
    id: 12,
    name: '周年庆典',
    url: 'https://picsum.photos/seed/anniversary/800/400.jpg',
    thumbnail: 'https://picsum.photos/seed/anniversary/200/100.jpg',
    category: '节日活动'
  }
]

// 按分类获取图片
export const getPresetImagesByCategory = (category) => {
  if (!category) return presetImages
  return presetImages.filter(image => image.category === category)
}

// 获取所有分类
export const getPresetImageCategories = () => {
  const categories = [...new Set(presetImages.map(image => image.category))]
  return categories
}

// 根据ID获取图片
export const getPresetImageById = (id) => {
  return presetImages.find(image => image.id === id)
}