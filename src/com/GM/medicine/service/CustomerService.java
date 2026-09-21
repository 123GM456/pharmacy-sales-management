package com.GM.medicine.service;

// 导入 ArrayList：关键字为空时返回空集合的实例化类型
import java.util.ArrayList;
// 导入 List：findAll 方法返回的客户集合类型
import java.util.List;
// 导入 CustomerDao：数据库访问通过它完成
import com.GM.medicine.dao.CustomerDao;
// 导入 Customer：业务方法操作的客户实体类型
import com.GM.medicine.pojo.entity.Customer;

/**
 * - 客户业务类
 * - 负责客户的业务规则处理，如新增客户时的手机号查重、客户信息完整性校验
 * - 介于界面层与 CustomerDao 之间，本类只做业务校验与规则判断，不编写 JDBC 代码
 */
public class CustomerService {

    // 数据访问对象，负责真正读写数据库，业务校验通过后才调用它
    private CustomerDao customerDao = new CustomerDao();

    /**
     * 新增客户，新增前校验姓名
     *
     * @param customer 待新增的客户对象
     * @return 新增成功返回 true；对象为空、姓名为空或插入失败返回 false
     */
    public boolean addCustomer(Customer customer) {
        if (customer == null) {
            System.out.println("新增客户失败：客户对象不能为空");
            return false;
        }
        // name、phone 两列都不允许为空，数据库也不接受空姓名或空电话
        if (customer.getName() == null || customer.getName().isEmpty()) {
            System.out.println("新增客户失败：客户姓名为空");
            return false;
        }
        if (customer.getPhone() == null || customer.getPhone().isEmpty()) {
            System.out.println("新增客户失败：联系电话为空");
            return false;
        }
        // phone 上只有普通索引，数据库不会拦重复，查重必须在 Service 层完成
        if (customerDao.findByPhone(customer.getPhone()) != null) {
            System.out.println("新增客户失败：联系电话已存在");
            return false;
        }
        boolean result = customerDao.add(customer);
        // 按插入结果显示不同的提示，便于控制台测试时确认本次操作是成功还是失败
        if (result) {
            System.out.println("新增客户成功：" + customer.getName());
        } else {
            System.out.println("新增客户失败：数据库写入失败");
        }
        return result;
    }

    /**
     * 更新客户信息，更新前校验编号与姓名
     *
     * @param customer 携带新值并包含 id 的客户对象
     * @return 更新成功返回 true；对象或编号为空、姓名为空或更新失败返回 false
     */
    public boolean updateCustomer(Customer customer) {
        if (customer == null || customer.getId() == null) {
            System.out.println("更新客户失败：客户对象或客户编号不能为空");
            return false;
        }
        // name 列不允许为空，空姓名会把原有姓名覆盖成 NULL，必须拦截
        if (customer.getName() == null || customer.getName().isEmpty()) {
            System.out.println("更新客户失败：客户姓名为空");
            return false;
        }
        // phone 列同样不允许为空，空电话会把原有联系电话覆盖成 NULL
        if (customer.getPhone() == null || customer.getPhone().isEmpty()) {
            System.out.println("更新客户失败：联系电话为空");
            return false;
        }
        boolean result = customerDao.update(customer);
        // 按更新结果显示不同的提示，便于控制台测试时确认本次操作是成功还是失败
        if (result) {
            System.out.println("更新客户成功：客户编号 " + customer.getId());
        } else {
            System.out.println("更新客户失败：客户不存在或数据库更新失败");
        }
        return result;
    }

    /**
     * 根据编号查询单个客户
     *
     * @param id 客户编号
     * @return 查询到的客户对象，编号为空或记录不存在时返回 null
     */
    public Customer findById(Integer id) {
        if (id == null) {
            System.out.println("查询客户失败：客户编号不能为空");
            return null;
        }
        Customer customer = customerDao.findById(id);
        if (customer == null) {
            System.out.println("查询客户失败：编号 " + id + " 不存在");
        } else {
            System.out.println("查询客户成功：" + customer);
        }
        return customer;
    }

       /**
     * 查询全部客户
     *
     * @return 客户列表，没有数据时为空集合
     */
    public List<Customer> findAll() {
        List<Customer> customers = customerDao.findAll();
        if (customers.isEmpty()) {
            System.out.println("查询所有客户失败：当前没有客户数据");
        } else {
            System.out.println("查询所有客户成功：共 " + customers.size() + " 条数据");
        }
        return customers;
    }

    /**
     * 根据联系电话查询客户
     *
     * @param phone 客户联系电话
     * @return 查询到的客户对象，联系电话为空或记录不存在时返回 null
     */
    public Customer findByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            System.out.println("查询客户失败：联系电话不能为空");
            return null;
        }
        Customer customer = customerDao.findByPhone(phone);
        if (customer == null) {
            System.out.println("查询客户失败：联系电话 " + phone + " 不存在");
        } else {
            System.out.println("查询客户成功：" + customer);
        }
        return customer;
    }
    
    /**
     * 根据姓名关键字模糊查询客户，允许只输入姓名中的一部分
     *
     * @param name 客户姓名关键字
     * @return 匹配到的客户列表，关键字为空或没有匹配记录时为空集合
     */
    public List<Customer> findByNameLike(String name) {
        // 关键字为空时无法确定查询范围，直接返回空集合
        if (name == null || name.isEmpty()) {
            System.out.println("模糊查询客户失败：姓名关键字不能为空");
            return new ArrayList<>();
        }
        List<Customer> customers = customerDao.findByNameLike(name);
        if (customers.isEmpty()) {
            System.out.println("模糊查询客户失败：没有匹配的客户");
        } else {
            System.out.println("模糊查询客户成功：共 " + customers.size() + " 条数据");
        }
        return customers;
    }

}