-- 更新orders表，添加缺失的用户删除和商家删除字段
ALTER TABLE orders ADD COLUMN IF NOT EXISTS user_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS merchant_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- 可选：更新现有订单的这两个字段为false，确保数据一致性
UPDATE orders SET user_deleted = FALSE WHERE user_deleted IS NULL;
UPDATE orders SET merchant_deleted = FALSE WHERE merchant_deleted IS NULL;
