import Link from 'next/link';
import { useRouter } from 'next/router';

const adminMenus = [
  { href: '/admin/reservations', label: '예약 관리' },
  { href: '/admin/schedule', label: '영업일 관리' },
  { href: '/admin/service-items', label: '시술 메뉴 관리' },
];

/** 관리자 기능을 한곳에 모아 화면 크기와 관계없이 페이지를 바로 이동하게 합니다. */
export default function AdminNavigation() {
  const router = useRouter();

  return (
    <nav className="admin-navigation" aria-label="관리자 메뉴">
      {adminMenus.map((menu) => (
        <Link
          key={menu.href}
          href={menu.href}
          className={router.pathname === menu.href ? 'active' : ''}
          aria-current={router.pathname === menu.href ? 'page' : undefined}
        >
          {menu.label}
        </Link>
      ))}
    </nav>
  );
}
