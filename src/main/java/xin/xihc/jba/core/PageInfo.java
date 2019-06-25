package xin.xihc.jba.core;

/**
 * 分页信息
 *
 * @author Leo.Xi
 * @date 2018年1月19日
 * @since
 */
public class PageInfo {

    private Integer pageNo = 1; // 当前页数
    private Integer pageSize = 10;// 每页数量
    private Integer totalCount = 0;// 总数量
    private Integer totalPage = 0;// 总页数
    private Boolean needTotalCount = true;// 是否需要查询总数量

    /**
     *
     */
    public PageInfo() {
        super();
    }

    /**
     * 构造方法
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     */
    public PageInfo(Integer pageNo, Integer pageSize) {
        super();
        setPageNo(pageNo);
        setPageSize(pageSize);
    }

    /**
     * 构造方法
     *
     * @param pageNo         页码
     * @param pageSize       每页大小
     * @param needTotalCount 是否需要查询总数量
     */
    public PageInfo(Integer pageNo, Integer pageSize, boolean needTotalCount) {
        super();
        setPageNo(pageNo);
        setPageSize(pageSize);
        setNeedTotalCount(needTotalCount);
    }

    /**
     * 获取分页的起始值
     *
     * @return 起始值
     * @author Leo.Xi
     * @date 2019/6/25
     * @since 0.0.1
     */
    public int getStart() {
        // 计算起始索引
        // 使用limit 0, 10分页 -- 索引从0开始
        return (this.pageNo - 1) * this.pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        if (this.pageSize < 1) {
            this.pageSize = 10;
        }
        totalPage = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        this.totalCount = totalCount;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        if (null == pageNo || pageNo < 1) {
            return;
        }
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if (null == pageSize || pageSize < 1) {
            return;
        }
        this.pageSize = pageSize;
    }

    public Boolean getNeedTotalCount() {
        return needTotalCount;
    }

    public void setNeedTotalCount(Boolean needTotalCount) {
        this.needTotalCount = needTotalCount;
    }
}
