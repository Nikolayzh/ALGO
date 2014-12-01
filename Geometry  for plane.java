import java.util.*;
import static java.lang.Math.*;

public class Geometry2D {
	
	public static void main(String[] args) {
		new Geometry2D().run();
	}
	
	double EPS = 1e-7;
	
	/* Тесты */
	void run() {
//		Circle2D c = new Circle2D(new Point2D(0, 0), 3);
//		Point2D p = new Point2D(-3, 0);
//		System.out.println(Arrays.toString(tpc(p, c)));
		
//		Line2D l = new Line2D(new Point2D(0, 0), new Point2D(10, 10));
//		System.out.println(l);
//		System.out.println(l.shift(10));
		
//		Circle2D c1 = new Circle2D(new Point2D(-2, 0), 1);
//		Circle2D c2 = new Circle2D(new Point2D(2, 0), 1);
//		System.out.println(Arrays.toString(intcc(c1, c2)));
//		System.out.println(Arrays.toString(outtcc(c1, c2)));
		
//		Random rnd = new Random(1120);
//		Point2D[] p = new Point2D [6];
//		for (int i = 0; i < 6; i++)
//			p[i] = new Point2D(rnd.nextDouble() * 10, rnd.nextDouble() * 10);
//		System.out.println(Arrays.toString(p));
//		System.out.println(Arrays.toString(greham(p)));
//		System.out.println(Arrays.toString(largestTriangle(p)));
	}
	
	/* Геометрия на плоскости */
	
	/* 01. Пересечения плоских фигур */
	
	/* Пересечение прямых (основано на методе Крамера) */
	Point2D cll(Line2D l1, Line2D l2) {
		double det = -det2x2(l1.A, l1.B, l2.A, l2.B);
		if (abs(det) < EPS)
			return null;
		return new Point2D(det2x2(l1.C, l1.B, l2.C, l2.B) / det, det2x2(l1.A, l1.C, l2.A, l2.C) / det);
	}
	
	/* Пересечение отрезков */
	Point2D css(Line2D s1, Line2D s2) {
		Point2D its = cll(s1, s2);
		return (its != null && s1.contains(its) && s2.contains(its)) ? its : null;
	}
	
	/* Пересечение прямой и окружности */
	Point2D[] clc(Line2D l, Circle2D c) {
		double d = l.dist(c.c);
		if (d > c.r + EPS)
			return null;
		Point2D h = l.projection(c.c);
		Point2D v = l.dir().normalize(sqrt(c.r * c.r - d * d));
		return new Point2D[] { h.sub(v), h.add(v) };
	}
	
	/* Пересечение окружностей */
	Point2D[] ccc(Circle2D c1, Circle2D c2) {
		double d = dist(c1.c, c2.c);
		if (d < EPS && c1.r < EPS && c2.r < EPS) // крайний случай
			return new Point2D[] { c1.c };
		if (d < abs(c1.r - c2.r) - EPS || d > c1.r + c2.r + EPS || d < EPS)
			return null;
		double cos = getCos(d, c1.r, c2.r);
		double sin = sqrt(1.0 - cos * cos);
		Point2D v = c2.c.sub(c1.c).normalize(c1.r);
		return new Point2D[] { c1.c.add(v.rotate(cos, sin)), c1.c.add(v.rotate(cos, -sin)) };
	}
	
	/* 02. Касательные */
	
	/* Касательная из точки на окружности */
	Line2D tcpc(Point2D p, Circle2D c) {
		if (c.r < EPS || !c.lay(p))
			return null;
		Point2D n = c.c.sub(p).turn90();
		return new Line2D(p.sub(n), p.add(n));
	}
	
	/* Касательные из точки к окружности */
	Line2D[] tpc(Point2D p, Circle2D c) {
		double d = dist(p, c.c);
		if (d < c.r - EPS || abs(d) < EPS)
			return null;
		if (d < c.r + EPS)
			return new Line2D[] { new Line2D(p, new Line2D(p, c.c)), new Line2D(p, new Line2D(c.c, p)) };
		double sin = c.r / d;
		double cos = sqrt(1.0 - sin * sin);
		Point2D v = c.c.sub(p).normalize(sqrt(d * d - c.r * c.r));
		return new Line2D[] { new Line2D(p, p.add(v.rotate(cos, sin))), new Line2D(p, p.add(v.rotate(cos, -sin))) };
	}
	
	/* Внутренние касательные между окружностями */
	Line2D[] intcc(Circle2D c1, Circle2D c2) {
		Line2D[] h = tpc(c2.c, new Circle2D(c1.c, c1.r + c2.r));
		if (h != null) {
			h[0].shift(-c1.r);
			h[1].shift(c1.r);
		}
		return h;
	}
	
	/* Внешние касательные между окружностями (c1.r <= c2.r) */
	Line2D[] outtcc(Circle2D c1, Circle2D c2) {
		if (c1.r > c2.r + EPS)
			return outtcc(c2, c1);
		Line2D[] h = tpc(c1.c, new Circle2D(c2.c, c2.r - c1.r));
		if (h != null) {
			h[0].shift(-c1.r);
			h[1].shift(c1.r);
		}
		return h;
	}
	
	/* 03. Площади */
	
	/* Площадь треугольника */
	double area(Point2D p1, Point2D p2, Point2D p3) {
		return 0.5 * abs(cross(p1, p2, p3));
	}
	
	/* Площадь многоугольника */
	double area(Point2D[] p) {
		if (p.length < 3)
			return 0.0;
		double ret = 0.0;
		for (int i = 0, j = 1; i < p.length; i++, j = (++j == p.length ? 0 : j))
			ret += p[i].crossProduct(p[j]);
		return 0.5 * ret;
	}
	
	/* 04. Выпуклая оболочка */
	
	/* Алгоритм Грэхэма */	
	Point2D[] greham(Point2D[] p) {
		int k = lowestRight(p);
		int n = p.length;
		for (Point2D cp : p) {
			cp.a = atan2(cp.y - p[k].y, cp.x - p[k].x);
			cp.d = dist(cp, p[k]);
		}
		p[k].a = -1;
		Arrays.sort(p, new GrahamCmp());
		Point2D[] res = new Point2D [n];
		k = -1;
		for (Point2D cp : p) {
			while (k > 0 && cross(res[k], res[k - 1], cp) > -EPS)
				k--;
			res[++k] = cp;
		}
		return Arrays.copyOf(res, k + 1);
	}
	
	int lowestRight(Point2D[] p) {
		int ret = 0;
		for (int i = 1; i < p.length; i++)
			if (p[i].y < p[ret].y - EPS || abs(p[i].y - p[ret].y) < EPS && p[i].x > p[ret].x)
				ret = i;
		return ret;
	}
	
	/* 05. Другое */
	
	/* Принадлежность многоугольнику */
	boolean insidePoly(Point2D p, Point2D[] poly) {
		double sum = 0.0;
		int n = poly.length;
		for (int i = 0; i < n; i++) {
			double add = getPolarAngle(poly[i], p) - getPolarAngle(poly[(i + 1 == n ? 0 : i + 1)], p);
			if (add > PI) add -= PI * 2;
			if (add < PI) add += PI * 2;
			sum += add;
		}
		return abs(abs(sum) - PI * 2) < EPS;
	}
	
	/* Возвращает наибольший треугольник построенный на данных точках O(n)  */
	Point2D[] largestTriangle(Point2D[] poly) {
		Point2D[] p = greham(enumerate(poly));
		int n = p.length, b0 = -1, b1 = -1, b2 = -1;
		if (n < 3)
			return null;
		double best = -1.0;
		for (int p0 = 0, p1 = 1, p2 = 2; p0 < n;) {
			for (;; p1 = (p1 + 1) % n) {
				while (area(p[p0], p[p1], p[p2]) < area(p[p0], p[p1], p[(p2 + 1) % n]) + EPS)
					p2 = (p2 + 1) % n;
				if (area(p[p0], p[p1], p[p2]) > area(p[p0], p[(p1 + 1) % n], p[p2]) + EPS)
					break;
			}
			double cur = area(p[p0], p[p1], p[p2]);
			if (best < cur) {
				best = cur;
				b0 = p0;
				b1 = p1;
				b2 = p2;
			}
			p0++;
			if (p1 == p0)
				p1 = (p1 + 1) % n;
			if (p2 == p1)
				p2 = (p2 + 1) % n;
		}
		return best < 0 ? null : new Point2D[] { p[b0], p[b1], p[b2] };
	}
	
	/* Возвращает пару самых дальних точек O(n) */
	Point2D[] farthestPoints(Point2D[] poly) {
		Point2D[] p = greham(enumerate(poly));
		int n = p.length;
		int k = 0;
		double md = 0.0;
		Line2D fl = new Line2D(p[0], p[1]);
		for (int i = 0; i < n; i++) {
			double cd = fl.dist(p[i]);
			if (md < cd) {
				md = cd;
				k = i;
			}
		}
		Point2D[] fp = new Point2D[] { dist(p[0], p[k]) > dist(p[1], p[k]) ? p[0] : p[1], p[k] };
		md = dist(fp[0], fp[1]);
		for (int i = 1; i < n; i++) {
			int j = (i + 1) % n;
			fl.set(p[i], p[j]);
			while (fl.dist(p[k]) < fl.dist(p[(k + 1) % n]))
				k = (k + 1) % n;
			if (md < dist(p[i], p[k])) {
				md = dist(p[i], p[k]);
				fp[1] = p[i];
				fp[2] = p[k];
			}
			if (md < dist(p[j], p[k])) {
				md = dist(p[j], p[k]);
				fp[1] = p[j];
				fp[2] = p[k];
			}
		}
		return fp;
	}
	
	/* Вспомогательные функции */

	/* Пронумеровывает точки */
	Point2D[] enumerate(Point2D[] poly) {
		for (int i = 0; i < poly.length; i++)
			poly[i].id = i;
		return poly;
	}

	/* x^2 */
	double sqr(double x) {
		return x * x;
	}
	
	/* Определитель матрицы 2x2 */ 
	double det2x2(double a11, double a12, double a21, double a22) {
		return a11 * a22 - a12 * a21;
	}
	
	/* Косое произведение по 3м точкам */
	double cross(Point2D p0, Point2D p1, Point2D p2) {
		return (p1.x - p0.x) * (p2.y - p0.y) - (p2.x - p0.x) * (p1.y - p0.y);
	}
	
	/* Теорема косинусов */
	double getCos(double a, double b, double c) {
		return 0.5 * (a * a + b * b - c * c) / (a * b);
	}
	
	/* Полярный угол p относительно c */
	double getPolarAngle(Point2D c, Point2D p) {
		return atan2(p.y - c.y, p.x - c.x);
	}
	
	/* |p1,p2|^2 */
	double distSqr(Point2D p1, Point2D p2) {
		return sqr(p2.x - p1.x) + sqr(p2.y - p1.y);
	}
	
	/* |p1,p2| */
	double dist(Point2D p1, Point2D p2) {
		return sqrt(distSqr(p1, p2));
	}
	
	/* a <= x <= b */
	boolean between(double a, double x, double b) {
		return a <= x && x <= b;
	}
	
	/* x в [a,b] */
	boolean range(double a, double x, double b) {
		return between(a - EPS, x, b + EPS);
	}
	
	/* x в (a,b) */
	boolean interval(double a, double x, double b) {
		return between(a + EPS, x, b - EPS);
	}
	
	/* Безопасный корень */
	double sqrt(double x) {
		check(x >= -EPS);
		return Math.sqrt(max(0.0, x));
	}
	
	/* Безопасный арккосинус */
	double acos(double x) {
		check(range(-1.0, x, 1.0));
		return Math.acos(max(-1.0, min(1.0, x)));
	}
	
	/* Безопасные арксинус */	
	double asin(double x) {
		check(range(-1.0, x, 1.0));
		return Math.asin(max(-1.0, min(1.0, x)));
	}
	
	/* Проверяет условие, и если оно не выполняется кидает исключение */
	void check(boolean state) {
		if (!state) {
			System.err.println(new Error().getStackTrace());
			throw new RuntimeException();
		}
	}
	
	/* Классы */
	
	/* Точка (вектор) на плоскости XOY */
	class Point2D {
		int id; // иногда нужно
		
		double x;
		double y;
		
		/* Для выпуклой оболочки */
		double d;
		double a;
		
		/* Тривиальный конструктор */
		Point2D() {
			x = y = 0.0;
		}
		
		/* Основной конструктор */
		Point2D(double x, double y) {
			this.set(x, y);
		}
		
		/* Задает координаты */
		Point2D set(double x, double y) {
			this.x = x;
			this.y = y;
			return this;
		}
		
		/* Возвращает копию */
		Point2D copy() {
			return new Point2D(x, y);
		}
		
		/* Сумма векторов */
		Point2D add(Point2D v) {
			return new Point2D(x + v.x, y + v.y);
		}
		
		/* Перемещает точку на заданный вектор */
		Point2D inc(Point2D v) {
			return set(x + v.x, y + v.y);
		}
		
		/* Разность векторов */
		Point2D sub(Point2D v) {
			return new Point2D(x - v.x, y - v.y);
		}
		
		/* Аналогично inc, но в обратном направлении */
		Point2D dec(Point2D v) {
			return set(x - v.x, y - v.y);
		}
		
		/* Произведение вектора на скаляр */
		Point2D mul(double K) {
			return new Point2D(K * x, K * y);
		}
		
		/* Масштабирует вектор */
		Point2D scale(double K) {
			return set(K * x, K * y);
		}
		
		/* Возвращает длину вектора */
		double len() {
			return sqrt(x * x + y * y);
		}
		
		/* Возращает нормированный вектор */		
		Point2D norm() {
			return norm(1.0);
		}
		
		/* Возращает нормированный (до заданной длины) вектор */
		Point2D norm(double newLen) {
			return this.mul(newLen / len());
		}
		
		/* Нормализует вектор */
		Point2D normalize() {
			return this.normalize(1.0);
		}
		
		/* Нормализует вектор до заданной длины */
		Point2D normalize(double newLen) {
			return scale(newLen / len());
		}
		
		/* Скалярное произведение */
		double scalarProduct(Point2D v) {
			return x * v.x + y * v.y;
		}
		
		/* Косое произведение */
		double crossProduct(Point2D v) {
			return x * v.y - v.x * y;
		}
		
		/* Возвращает вектор повернутый на 90 градусов (CCV) */
		Point2D rotate90() {
			return new Point2D(-y, x);
		}
		
		/* Возвращает вектор повернутый на angle радиан (CCV) */
		Point2D rotate(double angle) {
			double cos = cos(angle);
			double sin = sin(angle);
			return rotate(cos, sin);
		}
		
		/* Возвращает вектор повернутый по заданым косинусу и синусу (CCV) */
		Point2D rotate(double cos, double sin) {
			return new Point2D(x * cos - y * sin, x * sin + y * cos);
		}
		
		/* Поворачивает вектор на 90 градусов (CCV) */
		Point2D turn90() {
			return set(-y, x);
		}
		
		/* Поворачивает вектор на angle радиан (CCV) */
		Point2D turn(double angle) {
			double cos = cos(angle);
			double sin = sin(angle);
			return turn(cos, sin);
		}

		/* Поворачивает вектор по заданым косинусу и синусу (CCV) */
		Point2D turn(double cos, double sin) {
			return set(x * cos - y * sin, x * sin + y * cos);
		}

		@Override
		public boolean equals(Object obj) {
			Point2D p = (Point2D) obj;
			return abs(x - p.x) < EPS && abs(y - p.y) < EPS;
		}
		
		@Override
		public String toString() {
			return String.format(Locale.US ,"(%.3f, %.3f)", x, y);
		}
		
		@Override
		protected Object clone() throws CloneNotSupportedException {
			return this.copy();
		}
	}
	
	class GrahamCmp implements Comparator<Point2D> {
		@Override
		public int compare(Point2D p1, Point2D p2) {
			if (p1.a < p2.a - EPS)
				return -1;
			if (p1.a > p2.a + EPS)
				return 1;
			return p1.d < p2.d ? -1 : 1;
		}
	}
	
	/* Прямая (отрезок) на плоскости XOY */
	class Line2D {
		Point2D p1;
		Point2D p2;
		double A;
		double B;
		double C;
		
		/* Прямая по 2м точкам */
		Line2D(Point2D p1, Point2D p2) {
			this.set(p1.copy(), p2.copy());
		}

		/* Прямая через точку, перпендикулярно данной прямой */
		Line2D(Point2D p, Line2D l) {
			this.set(p.copy(), l);
		}
		
		/* Прямая параллельно данной на расстоянии r */
		Line2D(double r, Line2D l) {
			this.set(r, l);
		}
		
		Line2D set(Point2D p1, Point2D p2) {
			this.p1 = p1;
			this.p2 = p2;
			A = p2.y - p1.y;
			B = p1.x - p2.x;
			C = -(A * p1.x + B * p1.y);
			return this;
		}
		
		Line2D set(Point2D p, Line2D l) {
			p1 = p;
			p2 = p.add(l.normal());
			A = -l.B;
			B = l.A;
			C = -(A * p1.x + B * p1.y);
			return this;
		}
		
		Line2D set(double r, Line2D l) {
			Point2D v = l.normal().normalize(r);
			p1 = l.p1.sub(v);
			p2 = l.p2.sub(v);
			A = l.A;
			B = l.B;
			C = -(A * p1.x + B * p1.y);
			return this;
		}
		
		/* Параллельный перенос на вектор v */
		Line2D move(Point2D v) {
			p1.inc(v);
			p2.inc(v);
			C = -(A * p1.x + B * p1.y);
			return this;
		}
		
		/* Двигает по нормали на расстояние r */
		Line2D shift(double r) {
			Point2D v = this.normal().normalize(r);
			p1.dec(v);
			p2.dec(v);
			C = -(A * p1.x + B * p1.y);
			return this;
		}
		
		/* Подставляет точку в уравнение прямой */
		double calc(Point2D p) {
			return A * p.x + B * p.y + C;
		}
		
		/* Лежит ли точка на прямой */
		boolean lay(Point2D p) {
			return abs(this.calc(p)) < EPS;
		}
		
		/* Содержит ли отрезок прямую */
		boolean contains(Point2D p) {
			if (!lay(p))
				return false;
			return range(0.0, this.getT(p), 1.0);
		}
		
		/* Для точек лежащих на прямой возвращает параметр соответствуюущий им */
		double getT(Point2D p) {
//			check(this.lay(p));
			if (!this.lay(p))
				return Double.NaN;
			if (abs(A) > EPS)
				return (p.y - p1.y) / A;
			if (abs(B) > EPS)
				return (p1.x - p.x) / B;
			throw new RuntimeException("Bad line");
		}
		
		/* Возвращает точку по параметру */
		Point2D getPoint(double t) {
			return new Point2D(p1.x - B * t, p1.y + A * t);
		}
		
		/* Возвращает расстояние до точки */
		double dist(Point2D p) {
			return abs(this.calc(p)) / sqrt(A * A + B * B);
		}
		
		/* Вектор направления */
		Point2D dir() {
			return new Point2D(-B, A);
		}
		
		/* Вектор нормали */
		Point2D normal() {
			return new Point2D(A, B);
		}
		
//		Point2D projection(Point2D p) {
//			double t = (A * p.x + B * p.y + C) / (A * A + B * B);
//			return new Point2D(p.x - A * t, p.y - B * t);
//		}
		
		/* Проекция точки */
		Point2D projection(Point2D p) {
			double t = (B * (p1.x - p.x) - A * (p1.y - p.y)) / (A * A + B * B);
			return new Point2D(p1.x - B * t, p1.y + A * t);
		}
		
		/* Проекция отрезка */
		Line2D projection(Line2D l) {
			return new Line2D(this.projection(l.p1), this.projection(l.p2));
		}
		
		@Override
		public String toString() {
			return "[" + p1 + ", " + p2 + "], " + String.format(Locale.US, "%.3fx + %.3fy + %.3f = 0", A, B, C);
		}
	}
	
	/* Окружность с центром c и радиусом r */
	class Circle2D {
		Point2D c;
		double r;
		
		Circle2D(Point2D c, double r) {
			this.set(c.copy(), r);
		}

		Circle2D set(Point2D c, double r) {
			this.c = c;
			this.r = r;
			return this;
		}
		
		double area() {
			return PI * r * r;
		}
		
		boolean lay(Point2D p) {
			return range(r, dist(c, p), r);
		}
		
		boolean contains(Point2D p) {
			return dist(c, p) < r + EPS;
		}
	}
}
