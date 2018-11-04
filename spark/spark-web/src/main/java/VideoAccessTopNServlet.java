
import net.sf.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/27 18:38
 */
public class VideoAccessTopNServlet extends HttpServlet {

    private TopNDao topNDao;

    @Override
    public void init() throws ServletException {
        topNDao = new TopNDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String day = req.getParameter("day");
        List<VideoAccessTopN> list = topNDao.listVideoAccessTopN(day);
        JSONArray jsonArray = JSONArray.fromObject(list);
        resp.setContentType("text/html;charset=utf-8");
        System.out.println(jsonArray);
        resp.setCharacterEncoding("UTF8");
        PrintWriter writer = resp.getWriter();
        writer.write(jsonArray.toString());
        writer.flush();
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }
}
