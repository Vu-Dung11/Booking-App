#!/usr/bin/env node
/**
 * Generate scripts/seed-host01.sql — additive insert for host01@stay.vn.
 * INSERT ONLY, không TRUNCATE. Dùng AUTO_INCREMENT để tránh PK conflict.
 *
 * Counts (additive):
 *   properties +7, rooms +25, property_images +21, room_images +50,
 *   room_inventory +750 (25 new rooms x 30 days),
 *   bookings +60, payments +47, reviews +18
 *
 * Usage:
 *   node scripts/generate-seed-host01.js          # default (insert only)
 *   node scripts/generate-seed-host01.js --reset  # prepend DELETE statements
 */

const fs = require('fs');
const path = require('path');

const RESET = process.argv.includes('--reset');

// ---------- helpers ----------
const rand = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;
const pick = (arr) => arr[rand(0, arr.length - 1)];
const sqlStr = (s) => `'${String(s).replace(/'/g, "''")}'`;
const dateOffset = (days) => {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
};
const datetimeOffset = (daysOffset, hour) => {
  const d = new Date();
  d.setDate(d.getDate() + daysOffset);
  d.setHours(hour, rand(0, 59), rand(0, 59), 0);
  return d.toISOString().slice(0, 19).replace('T', ' ');
};

// ---------- catalogues ----------
const cityPlan = [
  { city: 'Đà Lạt', count: 3 },
  { city: 'Hội An', count: 2 },
  { city: 'Phú Quốc', count: 2 },
];

const addressesByCity = {
  'Đà Lạt': ['Đường Trần Hưng Đạo', 'Đường Nguyễn Công Trứ', 'Đường Lê Hồng Phong', 'Đường Khe Sanh', 'Đường Cao Bá Quát', 'Đường Trần Quốc Toản'],
  'Hội An': ['Đường Cửa Đại', 'Đường Hai Bà Trưng', 'Đường Nguyễn Du', 'Đường Trần Cao Vân', 'Đường Lý Thường Kiệt'],
  'Phú Quốc': ['Đường Cửa Cạn', 'Đường Hàm Ninh', 'Đường Suối Đá Bàn', 'Đường Bãi Ông Lang', 'Đường Gành Dầu'],
};

// Tên property MỚI - không trùng tên property cũ (đã check qua DB)
const newPropertyNames = {
  'Đà Lạt': [
    'Mây Ngàn Boutique Đà Lạt',
    'Tùng Lâm Garden Lodge',
    'Hoa Anh Đào Hillside',
    'Pinetree Retreat Đà Lạt',
    'Đồi Cù Riverside Stay',
  ],
  'Hội An': [
    'Faifo Mộc Garden Hội An',
    'Cẩm Châu Riverside Lodge',
    'An Mỹ Heritage Hội An',
    'Lantern Bay Hội An',
  ],
  'Phú Quốc': [
    'Bãi Ông Lang Coral Villa',
    'Sunset Cove Phú Quốc',
    'Hàm Ninh Fishing Lodge',
    'Pearl Bay Bungalow',
  ],
};

const roomTypes = [
  { type: 'Standard', cap: 2, priceMin: 350000, priceMax: 650000 },
  { type: 'Deluxe', cap: 2, priceMin: 700000, priceMax: 1300000 },
  { type: 'Family', cap: 4, priceMin: 1100000, priceMax: 2000000 },
  { type: 'Dorm', cap: 6, priceMin: 250000, priceMax: 400000 },
  { type: 'Suite', cap: 4, priceMin: 1700000, priceMax: 2800000 },
];

const positiveComments = [
  'Phòng đẹp, sạch sẽ, view tuyệt vời. Chủ nhà rất nhiệt tình và thân thiện.',
  'Trải nghiệm tuyệt vời, không gian yên tĩnh, đồ ăn ngon. Sẽ quay lại!',
  'Vị trí đắc địa, decor đẹp, dịch vụ chuyên nghiệp. Đáng đồng tiền bát gạo.',
  'Homestay đúng chuẩn 5 sao mini. Chủ host01 chăm khách rất chu đáo.',
  'Yên tĩnh, sạch, view núi/biển đẹp. Bữa sáng phong phú.',
  'Cơ sở vật chất mới, wifi nhanh, có hồ bơi mini. Highly recommended!',
  'Phù hợp cho gia đình và cặp đôi. Có nhiều góc check-in đẹp.',
  'Chủ nhà giới thiệu nhiều địa điểm hay, cho mượn xe đạp miễn phí.',
  'Decor tinh tế, ấm cúng, đậm chất Việt Nam. Cảm giác như ở nhà.',
  'Bữa sáng tự chọn ngon, view ban công cực đẹp lúc bình minh.',
  'Phục vụ chu đáo, giá hợp lý, sẽ giới thiệu cho bạn bè đồng nghiệp.',
  'Không gian xanh mát, có vườn nhỏ, gần chợ đêm. Rất tiện.',
  'Phòng rộng rãi, sạch tinh, drap khăn thơm tho. 10 điểm chất lượng.',
  'Trải nghiệm văn hóa địa phương qua bữa cơm gia đình. Cảm ơn chủ nhà!',
  'Đã đặt nhiều homestay nhưng đây là chỗ đáng nhớ nhất. 5 sao!',
  'Cảnh quan ngoạn mục, dịch vụ tận tâm. Quá xứng đáng để quay lại.',
  'Chủ nhà tâm lý, xử lý tình huống chuyên nghiệp. Wifi mạnh, điều hòa mát.',
  'Đáng từng đồng bỏ ra. Phòng đẹp hơn cả ảnh. Recommend mạnh!',
];

// ---------- generation ----------
let lines = [];
const W = (s) => lines.push(s);

W('-- ============================================================');
W('-- seed-host01.sql — additive data for host01@stay.vn');
W('-- Generated: ' + new Date().toISOString());
W('-- INSERT ONLY. Không xoá data hiện có.');
W('-- ');
W('-- ⚠️  Chạy nhiều lần sẽ duplicate booking/payment/review.');
W('-- Để re-run sạch, regenerate với --reset hoặc chạy DELETE block dưới đây:');
W('--   SET @h := (SELECT id FROM users WHERE email=\'host01@stay.vn\');');
W('--   DELETE FROM reviews WHERE booking_id IN (SELECT b.id FROM bookings b');
W('--     JOIN rooms r ON b.room_id=r.id JOIN properties p ON r.property_id=p.id');
W('--     WHERE p.host_id=@h);');
W('--   DELETE FROM payments WHERE booking_id IN (SELECT b.id FROM bookings b');
W('--     JOIN rooms r ON b.room_id=r.id JOIN properties p ON r.property_id=p.id');
W('--     WHERE p.host_id=@h);');
W('--   DELETE b FROM bookings b JOIN rooms r ON b.room_id=r.id');
W('--     JOIN properties p ON r.property_id=p.id WHERE p.host_id=@h;');
W('--   DELETE ri FROM room_inventory ri JOIN rooms r ON ri.room_id=r.id');
W('--     JOIN properties p ON r.property_id=p.id WHERE p.host_id=@h;');
W('--   DELETE rim FROM room_images rim JOIN rooms r ON rim.room_id=r.id');
W('--     JOIN properties p ON r.property_id=p.id WHERE p.host_id=@h;');
W('--   DELETE pim FROM property_images pim JOIN properties p ON pim.property_id=p.id');
W('--     WHERE p.host_id=@h;');
W('--   DELETE r FROM rooms r JOIN properties p ON r.property_id=p.id');
W('--     WHERE p.host_id=@h;');
W('--   DELETE FROM properties WHERE host_id=@h;');
W('-- ============================================================');
W('SET NAMES utf8mb4;');
W('');
W("SET @host_id := (SELECT id FROM users WHERE email='host01@stay.vn');");
W("SELECT IF(@host_id IS NULL, 'ERROR: host01@stay.vn not found, abort!', CONCAT('host_id resolved to: ', @host_id)) AS check_host;");
W('');

if (RESET) {
  W('-- ===== --reset: xoá toàn bộ data của host01 trước khi insert =====');
  W('DELETE FROM reviews WHERE booking_id IN (SELECT b.id FROM bookings b JOIN rooms r ON b.room_id=r.id JOIN properties p ON r.property_id=p.id WHERE p.host_id=@host_id);');
  W('DELETE FROM payments WHERE booking_id IN (SELECT b.id FROM bookings b JOIN rooms r ON b.room_id=r.id JOIN properties p ON r.property_id=p.id WHERE p.host_id=@host_id);');
  W('DELETE b FROM bookings b JOIN rooms r ON b.room_id=r.id JOIN properties p ON r.property_id=p.id WHERE p.host_id=@host_id;');
  W('DELETE ri FROM room_inventory ri JOIN rooms r ON ri.room_id=r.id JOIN properties p ON r.property_id=p.id WHERE p.host_id=@host_id;');
  W('DELETE rim FROM room_images rim JOIN rooms r ON rim.room_id=r.id JOIN properties p ON r.property_id=p.id WHERE p.host_id=@host_id;');
  W('DELETE pim FROM property_images pim JOIN properties p ON pim.property_id=p.id WHERE p.host_id=@host_id;');
  W('DELETE r FROM rooms r JOIN properties p ON r.property_id=p.id WHERE p.host_id=@host_id;');
  W('DELETE FROM properties WHERE host_id=@host_id;');
  W('');
}

// ===== PROPERTIES =====
// Strategy: dùng AUTO_INCREMENT. Tạo 1 INSERT/property để LAST_INSERT_ID() lấy được id.
// Sau đó user-defined variables @prop1_id ... @prop7_id giữ id để dùng cho rooms.
W('-- ===== properties (+7) =====');
const propertyPlan = [];
let propIdx = 0;
for (const cp of cityPlan) {
  const namePool = [...newPropertyNames[cp.city]].sort(() => Math.random() - 0.5);
  for (let i = 0; i < cp.count; i++) {
    propIdx++;
    const name = namePool[i];
    const address = `${rand(1, 300)} ${pick(addressesByCity[cp.city])}, ${cp.city}`;
    const description = `${name} — homestay được host01 vận hành, không gian sang trọng và ấm cúng tại ${cp.city}. Đầy đủ tiện nghi cao cấp, gần các điểm tham quan nổi tiếng. Phù hợp cho gia đình, cặp đôi và nhóm bạn muốn nghỉ dưỡng đẳng cấp.`;
    propertyPlan.push({ idx: propIdx, name, description, address, city: cp.city, country: 'Vietnam' });
  }
}

for (const p of propertyPlan) {
  W(`INSERT INTO properties (host_id, name, description, address, city, country, is_active) VALUES (@host_id, ${sqlStr(p.name)}, ${sqlStr(p.description)}, ${sqlStr(p.address)}, ${sqlStr(p.city)}, ${sqlStr(p.country)}, 1);`);
  W(`SET @prop${p.idx}_id := LAST_INSERT_ID();`);
}
W('');

// ===== PROPERTY_IMAGES (+21, 3/property, 1 thumbnail) =====
W('-- ===== property_images (+21, 3/property) =====');
for (const p of propertyPlan) {
  for (let i = 1; i <= 3; i++) {
    const url = `https://res.cloudinary.com/dvx8eohft/image/upload/v1700000000/host01/property_${p.idx}_${i}.jpg`;
    const publicId = `host01/property_${p.idx}_${i}`;
    const isThumb = i === 1 ? 1 : 0;
    W(`INSERT INTO property_images (property_id, image_url, public_id, is_thumbnail) VALUES (@prop${p.idx}_id, ${sqlStr(url)}, ${sqlStr(publicId)}, ${isThumb});`);
  }
}
W('');

// ===== ROOMS (+25, 3-4 rooms/property, đa dạng roomType) =====
W('-- ===== rooms (+25, 3-4 rooms per new property) =====');
// 7 properties x base 3 rooms = 21, + 4 properties get 1 extra = 25
const roomPlan = [];
const extraTargets = new Set();
while (extraTargets.size < 4) extraTargets.add(rand(1, 7));

let roomCounter = 0;
for (const p of propertyPlan) {
  const roomCount = 3 + (extraTargets.has(p.idx) ? 1 : 0);
  // pick distinct room types per property
  const types = [...roomTypes].sort(() => Math.random() - 0.5).slice(0, roomCount);
  for (let i = 0; i < roomCount; i++) {
    roomCounter++;
    const rt = types[i];
    const price = Math.round(rand(rt.priceMin, rt.priceMax) / 50000) * 50000;
    const quantity = rand(2, 5);
    roomPlan.push({
      idx: roomCounter,
      propertyIdx: p.idx,
      roomType: rt.type,
      capacity: rt.cap,
      basePrice: price,
      quantity,
    });
  }
}

for (const r of roomPlan) {
  W(`INSERT INTO rooms (property_id, room_type, capacity, base_price, quantity) VALUES (@prop${r.propertyIdx}_id, ${sqlStr(r.roomType)}, ${r.capacity}, ${r.basePrice}, ${r.quantity});`);
  W(`SET @room${r.idx}_id := LAST_INSERT_ID();`);
}
W('');

// ===== ROOM_IMAGES (+50, 2/room) =====
W('-- ===== room_images (+50, 2 per new room) =====');
for (const r of roomPlan) {
  for (let i = 1; i <= 2; i++) {
    const url = `https://res.cloudinary.com/dvx8eohft/image/upload/v1700000000/host01/room_${r.idx}_${i}.jpg`;
    const publicId = `host01/room_${r.idx}_${i}`;
    const isThumb = i === 1 ? 1 : 0;
    W(`INSERT INTO room_images (room_id, image_url, public_id, is_thumbnail) VALUES (@room${r.idx}_id, ${sqlStr(url)}, ${sqlStr(publicId)}, ${isThumb});`);
  }
}
W('');

// ===== ROOM_INVENTORY (25 rooms x 30 days = 750) — chỉ cho rooms mới =====
W('-- ===== room_inventory (+750: 25 new rooms x 30 days) =====');
for (const r of roomPlan) {
  for (let day = 0; day < 30; day++) {
    W(`INSERT INTO room_inventory (room_id, inventory_date, available_count) VALUES (@room${r.idx}_id, '${dateOffset(day)}', ${r.quantity});`);
  }
}
W('');

// ===== BOOKINGS (+60) =====
// Reference rooms thuộc TẤT CẢ properties của host01 (cũ và mới).
// Cũ: dùng SELECT subquery -> insert 1 dòng/booking với các SELECT bind.
// Strategy: dùng INSERT ... SELECT để bind ngẫu nhiên room thuộc host01.
//
// Đơn giản hoá: sử dụng @candidate_rooms_csv không khả thi trong MySQL,
// nên ta sẽ dùng cách: với mỗi booking, chọn ngẫu nhiên room theo
// SELECT id FROM rooms r JOIN properties p ON ... WHERE p.host_id=@host_id
// ORDER BY RAND() LIMIT 1.
// base_price + quantity sẽ lookup tại insert time.
//
// Để có thể tính total_price = base_price * room_quantity * nights,
// ta lưu room_id vào @booking_room_id := (SELECT id FROM rooms ... LIMIT 1)
// rồi tính total_price từ subquery.

W('-- ===== bookings (+60) =====');
W('-- Chọn random room thuộc host01 (cũ + mới); total_price tính từ rooms.base_price.');
W('');

const guestPickSql = '(SELECT id FROM users WHERE role=\'GUEST\' ORDER BY RAND() LIMIT 1)';
const roomPickSql = '(SELECT r.id FROM rooms r JOIN properties p ON r.property_id=p.id WHERE p.host_id=@host_id ORDER BY RAND() LIMIT 1)';

const statusPlan = [
  ...Array(8).fill('PENDING'),
  ...Array(25).fill('CONFIRMED'),
  ...Array(22).fill('COMPLETED'),
  ...Array(5).fill('CANCELLED'),
];
// shuffle so they intermix in id order
statusPlan.sort(() => Math.random() - 0.5);

// We'll track which booking ids correspond to COMPLETED / CONFIRMED+COMPLETED
// for downstream payments and reviews. Use SQL variables @bk_<i>_id and flags.
let bookingCounter = 0;
const bookingMeta = []; // {idx, status, nights, roomQty}

for (const status of statusPlan) {
  bookingCounter++;
  const nights = rand(1, 5);
  const roomQty = rand(1, 2);

  let checkIn, checkOut, createdAt;
  if (status === 'COMPLETED') {
    const offset = -rand(7, 60);
    checkIn = dateOffset(offset);
    checkOut = dateOffset(offset + nights);
    createdAt = datetimeOffset(offset - rand(3, 30), rand(8, 22));
  } else if (status === 'CONFIRMED') {
    const offset = rand(1, 50);
    checkIn = dateOffset(offset);
    checkOut = dateOffset(offset + nights);
    createdAt = datetimeOffset(-rand(0, 14), rand(8, 22));
  } else if (status === 'PENDING') {
    const offset = rand(2, 30);
    checkIn = dateOffset(offset);
    checkOut = dateOffset(offset + nights);
    createdAt = datetimeOffset(-rand(0, 13), rand(8, 22));
  } else {
    const offset = rand(-30, 30);
    checkIn = dateOffset(offset);
    checkOut = dateOffset(offset + nights);
    createdAt = datetimeOffset(offset - rand(2, 20), rand(8, 22));
  }

  bookingMeta.push({ idx: bookingCounter, status, nights, roomQty, checkIn, checkOut, createdAt });

  // Step: pick room id, then insert booking. total_price = (base_price * roomQty * nights)
  W(`SET @bk${bookingCounter}_room_id := ${roomPickSql};`);
  W(`SET @bk${bookingCounter}_guest_id := ${guestPickSql};`);
  W(`SET @bk${bookingCounter}_total := (SELECT base_price FROM rooms WHERE id=@bk${bookingCounter}_room_id) * ${roomQty} * ${nights};`);
  W(`INSERT INTO bookings (guest_id, room_id, check_in_date, check_out_date, total_price, status, created_at, room_quantity) VALUES (@bk${bookingCounter}_guest_id, @bk${bookingCounter}_room_id, '${checkIn}', '${checkOut}', @bk${bookingCounter}_total, '${status}', '${createdAt}', ${roomQty});`);
  W(`SET @bk${bookingCounter}_id := LAST_INSERT_ID();`);
}
W('');

// ===== PAYMENTS (+47, cho CONFIRMED + COMPLETED) =====
W('-- ===== payments (+47, for CONFIRMED + COMPLETED) =====');
const paidBookings = bookingMeta.filter(b => b.status === 'CONFIRMED' || b.status === 'COMPLETED');
for (const b of paidBookings) {
  const txn = 'VNP' + String(rand(10000000000000, 99999999999999));
  W(`INSERT INTO payments (booking_id, amount, payment_method, status, transaction_id, created_at) VALUES (@bk${b.idx}_id, @bk${b.idx}_total, 'VNPAY', 'SUCCESS', '${txn}', '${b.createdAt}');`);
}
W('');

// ===== REVIEWS (+18, cho 18 trong 22 COMPLETED) =====
W('-- ===== reviews (+18, for first 18 of COMPLETED bookings) =====');
const completed = bookingMeta.filter(b => b.status === 'COMPLETED').slice(0, 18);
for (const b of completed) {
  const rating = rand(4, 5);
  const comment = pick(positiveComments);
  // review created_at = checkout + 1-7 days
  const co = new Date(b.checkOut);
  co.setDate(co.getDate() + rand(1, 7));
  co.setHours(rand(8, 22), rand(0, 59), 0, 0);
  const createdAt = co.toISOString().slice(0, 19).replace('T', ' ');
  // property_id = property của room đó
  W(`INSERT INTO reviews (booking_id, property_id, rating, comment, created_at) VALUES (@bk${b.idx}_id, (SELECT property_id FROM rooms WHERE id=@bk${b.idx}_room_id), ${rating}, ${sqlStr(comment)}, '${createdAt}');`);
}
W('');

// ===== Final report =====
W('-- ===== summary report =====');
W(`SELECT 'host_id' AS info, @host_id AS val;`);
W(`
SELECT 'properties' AS tbl, COUNT(*) AS total_for_host01
  FROM properties WHERE host_id=@host_id
UNION ALL SELECT 'rooms', COUNT(*) FROM rooms r
  JOIN properties p ON r.property_id=p.id WHERE p.host_id=@host_id
UNION ALL SELECT 'bookings', COUNT(*) FROM bookings b
  JOIN rooms r ON b.room_id=r.id JOIN properties p ON r.property_id=p.id
  WHERE p.host_id=@host_id
UNION ALL SELECT 'payments', COUNT(*) FROM payments py
  JOIN bookings b ON py.booking_id=b.id
  JOIN rooms r ON b.room_id=r.id JOIN properties p ON r.property_id=p.id
  WHERE p.host_id=@host_id
UNION ALL SELECT 'reviews', COUNT(*) FROM reviews rv
  JOIN bookings b ON rv.booking_id=b.id
  JOIN rooms r ON b.room_id=r.id JOIN properties p ON r.property_id=p.id
  WHERE p.host_id=@host_id
UNION ALL SELECT 'total_revenue (CONFIRMED+COMPLETED)',
  COALESCE(SUM(b.total_price), 0)
  FROM bookings b JOIN rooms r ON b.room_id=r.id
  JOIN properties p ON r.property_id=p.id
  WHERE p.host_id=@host_id AND b.status IN ('CONFIRMED','COMPLETED');
`);
W(`
SELECT p.name AS top_property, COUNT(b.id) AS booking_count,
  COALESCE(SUM(CASE WHEN b.status IN ('CONFIRMED','COMPLETED') THEN b.total_price ELSE 0 END), 0) AS revenue
FROM properties p
LEFT JOIN rooms r ON r.property_id=p.id
LEFT JOIN bookings b ON b.room_id=r.id
WHERE p.host_id=@host_id
GROUP BY p.id, p.name
ORDER BY booking_count DESC, revenue DESC
LIMIT 3;
`);

const out = path.join(__dirname, 'seed-host01.sql');
fs.writeFileSync(out, lines.join('\n'), 'utf8');
console.log(`Wrote ${out} (${lines.length} lines, RESET=${RESET})`);
