package tmall.servlet;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.HtmlUtils;

import comparator.ProductAllComparator;
import comparator.ProductDateComparator;
import comparator.ProductPriceComparator;
import comparator.ProductReviewComparator;
import comparator.ProductSaleCountComparator;
import tmall.bean.Category;
import tmall.bean.Product;
import tmall.bean.ProductImage;
import tmall.bean.PropertyValue;
import tmall.bean.Review;
import tmall.bean.User;
import tmall.dao.CategoryDAO;
import tmall.dao.ProductDAO;
import tmall.dao.ProductImageDAO;
import tmall.util.Page;

public class ForeServlet extends BaseForeServlet {
	
	public String home(HttpServletRequest request, HttpServletResponse response, Page page){
		List<Category> cs = new CategoryDAO().list();
		new ProductDAO().fill(cs);
		new ProductDAO().fillByRow(cs);
		request.setAttribute("cs", cs);
		return "home.jsp";
	}

	public String register(HttpServletRequest request, HttpServletResponse response, Page page){
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		name = HtmlUtils.htmlEscape(name);
		System.out.println(name);
		boolean exist = userDAO.isExist(name);
		if(exist){
			request.setAttribute("msg", "用户名已经被使用，不能使用");
			return "register.jsp";
		}
		User user = new User();
		user.setName(name);
		user.setPassword(password);
		userDAO.add(user);
		
		return "@registerSuccess.jsp";
	}
	
	public String login(HttpServletRequest request, HttpServletResponse response, Page page){
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		name = HtmlUtils.htmlEscape(name);
		User user = userDAO.get(name, password);
		if(user == null){
			request.setAttribute("msg", "账号密码错误");
			return "login.jsp";
		}
		request.getSession().setAttribute("user", user);
		return "@forehome";
	}
	
	public String logout(HttpServletRequest request, HttpServletResponse response, Page page){
		request.getSession().removeAttribute("user");
		return "@forehome";
	}
	
	public String product(HttpServletRequest request, HttpServletResponse response, Page page){
		int pid = Integer.parseInt(request.getParameter("pid"));
		Product p = productDAO.get(pid);
		
		List<ProductImage> productSingleImages = productImageDAO.list(p, ProductImageDAO.type_single);
		List<ProductImage> productDetailImages = productImageDAO.list(p, ProductImageDAO.type_detail);
		p.setProductSingleImages(productSingleImages);
		p.setProductDetailImages(productDetailImages);
		
		List<PropertyValue> pvs = propertyValueDAO.list(p.getId());
		List<Review> reviews = reviewDAO.list(pid);
		productDAO.setSaleAndReviewNumber(p);
		
		request.setAttribute("pvs", pvs);
		request.setAttribute("reviews", reviews);
		request.setAttribute("p", p);
		return "product.jsp";
	}
	
	public String checkLogin(HttpServletRequest request, HttpServletResponse response, Page page){
		User user = (User) request.getSession().getAttribute("user");
		if(user != null){
			return "%success";
		}
		return "%fail";
	}
	
	public String loginAjax(HttpServletRequest request, HttpServletResponse response, Page page){
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		User user = userDAO.get(name, password);
		if(user == null){
			return "%fail";
		}
		return "%success";
	}
	
	public String category(HttpServletRequest request, HttpServletResponse response, Page page){
		int cid = Integer.parseInt(request.getParameter("cid"));
		Category c = new CategoryDAO().get(cid);
		new ProductDAO().fill(c);
		new ProductDAO().setSaleAndReviewNumber(c.getProducts());
		
		String sort = request.getParameter("sort");
		if(sort != null){
			switch(sort){
			case "review":
				Collections.sort(c.getProducts(), new ProductReviewComparator());
				break;
			case "date":
				Collections.sort(c.getProducts(), new ProductDateComparator());
				break;
			case "saleCount":
				Collections.sort(c.getProducts(), new ProductSaleCountComparator());
				break;
			case "price":
				Collections.sort(c.getProducts(), new ProductPriceComparator());
				break;
			case "all":
				Collections.sort(c.getProducts(), new ProductAllComparator());
				break;
			}
		}
		request.setAttribute("c", c);
		return "category.jsp";
	}
}
