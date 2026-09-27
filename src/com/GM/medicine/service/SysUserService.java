package com.GM.medicine.service;

// 导入 BCrypt：jBCrypt 库提供的成熟哈希算法，用于密码加密存储与验证
import org.mindrot.jbcrypt.BCrypt;
// 导入 ArrayList：无权限时返回空集合的实例化类型
import java.util.ArrayList;
// 导入 HashSet：记录已尝试过的员工号码，避免重复查库与死循环
import java.util.HashSet;
// 导入 Random：随机生成员工用户名号码
import java.util.Random;
// 导入 Set：已尝试号码集合的类型
import java.util.Set;
// 导入 List：findAll 方法返回的用户集合类型
import java.util.List;
// 导入 SysUserDao：业务方法通过它读写 sys_user 表
import com.GM.medicine.dao.SysUserDao;
// 导入 SysUserPasswordDTO：修改自己密码时接收新旧密码的传输对象
import com.GM.medicine.pojo.dto.SysUserPasswordDTO;
// 导入 SysUser：所有业务方法操作的用户实体类型
import com.GM.medicine.pojo.entity.SysUser;

/**
 * - 用户业务类
 * - 负责登录校验、个人信息维护、管理员用户管理、密码修改与重置等业务规则处理
 * - 介于界面层与 SysUserDao 之间，界面层不直接访问 DAO
 * - 权限基于 SysUser.role 区分：1 管理员，0 普通用户，不引入额外权限表
 */
public class SysUserService {

    // 新增用户的初始明文密码，入库前统一经 BCrypt 哈希
    private static final String DEFAULT_PASSWORD = "123456";

    // 管理员的固定用户名：系统只此一个管理员账号，保留前导 0 按 String 处理
    private static final String ADMIN_USERNAME = "00000";

    // 数据访问对象，负责真正读写数据库，业务规则校验通过后才调用它
    private SysUserDao sysUserDao = new SysUserDao();

    /**
     * 用户登录：按用户名查询用户，再依次校验密码与账号状态
     *
     * @param userName 登录用户名
     * @param password 登录密码
     * @return 登录成功返回对应用户对象；用户名不存在、密码错误或账号被禁用返回 null
     */
    public SysUser login(String userName, String password) {
        // 用户名或密码为空的请求不可能成功，直接返回 null，省去数据库查询
        if (userName == null || userName.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("登录失败：用户名或密码为空");
            return null;
        }
        // 先查用户，查不到说明用户名错误
        SysUser sysUser = sysUserDao.findByUserName(userName);
        if (sysUser == null) {
            System.out.println("登录失败：用户名不存在");
            return null;
        }
        // 用 BCrypt 验证明文密码与库中哈希是否匹配，禁止用 equals 直接比较
        if (!verifyPassword(password, sysUser.getPassword())) {
            System.out.println("登录失败：密码错误");
            return null;
        }
        // status 为 1 表示启用，其他值（含 0 禁用）一律拒绝登录
        if (sysUser.getStatus() == null || sysUser.getStatus() != SysUser.STATUS_ENABLED) {
            System.out.println("登录失败：账号已被禁用");
            return null;
        }
        System.out.println("登录成功：" + sysUser);
        return sysUser;
    }

    /**
     * 查询当前用户自己的信息，管理员与普通用户均可调用
     *
     * @param currentUser 当前登录用户
     * @return 当前用户对象；未登录或账号已不存在返回 null
     */
    public SysUser findMyInfo(SysUser currentUser) {
        // 未登录时没有可查询的对象
        if (currentUser == null || currentUser.getId() == null) {
            System.out.println("查询个人信息失败：当前用户未登录");
            return null;
        }
        // 只按当前用户自己的编号查询，不接受外部传入的编号，避免越权查看他人信息
        SysUser sysUser = sysUserDao.findById(currentUser.getId());
        if (sysUser == null) {
            System.out.println("查询个人信息：当前账号已不存在");
        } else {
            System.out.println("查询个人信息：" + sysUser);
        }
        return sysUser;
    }

    /**
     * 修改当前用户自己的个人信息，管理员与普通用户均可调用
     * - 只允许修改真实姓名与手机号，编号、用户名、密码、角色、状态一律不变
     * - 修改对象固定为 currentUser.getId()，防止普通用户改到他人资料
     *
     * @param currentUser 当前登录用户
     * @param updateInfo  携带新真实姓名与新手机号的对象
     * @return 修改成功返回 true；未登录、字段为空或更新失败返回 false
     */
    public boolean updateMyInfo(SysUser currentUser, SysUser updateInfo) {
        if (currentUser == null || currentUser.getId() == null) {
            System.out.println("修改个人信息失败：当前用户未登录");
            return false;
        }
        if (updateInfo == null || updateInfo.getId() == null) {
            System.out.println("修改个人信息失败：修改数据或用户 ID 不能为空"); 
            return false;
        }
        if (updateInfo.getRealName() == null || updateInfo.getRealName().isEmpty()) {
            System.out.println("修改个人信息失败：真实姓名为空");
            return false;
        }
        if (updateInfo.getPhone() == null || updateInfo.getPhone().isEmpty()) {
            System.out.println("修改个人信息失败：手机号为空");
            return false;
        }
        // 重新组装 DAO 参数：编号强制取当前用户，只带上允许修改的两个字段
        SysUser param = new SysUser();
        param.setId(currentUser.getId());
        param.setRealName(updateInfo.getRealName());
        param.setPhone(updateInfo.getPhone());
        boolean result = sysUserDao.update(param);
        if (result) {
            System.out.println("修改个人信息成功：" + param.getRealName());
        } else {
            System.out.println("修改个人信息失败：数据库更新失败");
        }
        return result;
    }

    /**
     * 修改当前用户自己的密码，管理员与普通用户均可调用
     * - 待修改编号固定取当前登录用户，DTO 中不携带编号，避免越权改他人密码
     *
     * @param currentUser 当前登录用户
     * @param passwordDTO 封装旧密码与新密码的传输对象
     * @return 修改成功返回 true；未登录、旧密码错误、新密码不合法或更新失败返回 false
     */
    public boolean changePassword(SysUser currentUser, SysUserPasswordDTO passwordDTO) {
        if (currentUser == null || currentUser.getId() == null) {
            System.out.println("修改密码失败：当前用户未登录");
            return false;
        }
        if (passwordDTO == null) {
            System.out.println("修改密码失败：修改密码数据为空");
            return false;
        }
        // 从数据库取当前用户，后续校验都要用到它库里的旧密码
        SysUser sysUser = sysUserDao.findById(currentUser.getId());
        if (sysUser == null) {
            System.out.println("修改密码失败：当前账号已不存在");
            return false;
        }
        // 旧密码错误说明不是本人在操作，直接拒绝；用 BCrypt 比对，不做明文比较
        if (!verifyPassword(passwordDTO.getOldPassword(), sysUser.getPassword())) {
            System.out.println("修改密码失败：旧密码错误");
            return false;
        }
        if (passwordDTO.getNewPassword() == null || passwordDTO.getNewPassword().isEmpty()) {
            System.out.println("修改密码失败：新密码为空");
            return false;
        }
        // 新旧密码相同等于没改，视为无效请求；BCrypt 每次加盐不同，只能用 matches 判断
        if (verifyPassword(passwordDTO.getNewPassword(), sysUser.getPassword())) {
            System.out.println("修改密码失败：新密码与旧密码相同");
            return false;
        }
        // 明文新密码先哈希再入库，控制台与数据库都不出现明文
        boolean result = sysUserDao.updatePassword(sysUser.getId(), hashPassword(passwordDTO.getNewPassword()));
        if (result) {
            System.out.println("修改密码成功：用户 " + sysUser.getUserName());
        } else {
            System.out.println("修改密码失败：数据库更新失败");
        }
        return result;
    }

    /**
     * 管理员查询指定用户的信息
     *
     * @param currentUser 当前登录用户
     * @param id          待查询的用户编号
     * @return 查询到的用户对象；无权限、编号为空或用户不存在返回 null
     */
    public SysUser findById(SysUser currentUser, Integer id) {
        if (!isAdmin(currentUser)) {
            System.out.println("查询用户失败：没有管理员权限");
            return null;
        }
        if (id == null) {
            System.out.println("查询用户失败：编号为空");
            return null;
        }
        SysUser sysUser = sysUserDao.findById(id);
        if (sysUser == null) {
            System.out.println("查询用户：编号 " + id + " 不存在");
        } else {
            System.out.println("查询用户：" + sysUser);
        }
        return sysUser;
    }

    /**
     * 管理员查询全部用户
     *
     * @param currentUser 当前登录用户
     * @return 用户列表；无权限时返回空集合，没有数据时同样为空集合
     */
    public List<SysUser> findAll(SysUser currentUser) {
        if (!isAdmin(currentUser)) {
            System.out.println("查询全部用户失败：没有管理员权限");
            return new ArrayList<>();
        }
        List<SysUser> sysUserList = sysUserDao.findAll();
        if (sysUserList.isEmpty()) {
            System.out.println("查询全部用户：当前没有用户数据");
        } else {
            System.out.println("查询全部用户，共 " + sysUserList.size() + " 条：" + sysUserList);
        }
        return sysUserList;
    }

    /**
     * 管理员按角色查询用户（用户管理页"管理员/员工"分类用）
     *
     * @param currentUser 当前登录用户
     * @param role        角色值：ROLE_ADMIN（1）或 ROLE_STAFF（0）
     * @return 用户列表；无权限时返回空集合，没有数据时同样为空集合
     */
    public List<SysUser> findByRole(SysUser currentUser, int role) {
        if (!isAdmin(currentUser)) {
            System.out.println("按角色查询用户失败：没有管理员权限");
            return new ArrayList<>();
        }
        List<SysUser> sysUserList = sysUserDao.findByRole(role);
        System.out.println("按角色查询用户，共 " + sysUserList.size() + " 条");
        return sysUserList;
    }

    /**
     * 管理员按状态查询用户（用户管理页"禁用用户"分类用）
     *
     * @param currentUser 当前登录用户
     * @param status      状态值：STATUS_ENABLED（1）或 STATUS_DISABLED（0）
     * @return 用户列表；无权限时返回空集合，没有数据时同样为空集合
     */
    public List<SysUser> findByStatus(SysUser currentUser, int status) {
        if (!isAdmin(currentUser)) {
            System.out.println("按状态查询用户失败：没有管理员权限");
            return new ArrayList<>();
        }
        List<SysUser> sysUserList = sysUserDao.findByStatus(status);
        System.out.println("按状态查询用户，共 " + sysUserList.size() + " 条");
        return sysUserList;
    }

    /**
     * 管理员按关键词模糊查询用户（用户管理页关键词查询用）
     *
     * @param currentUser 当前登录用户
     * @param keyword     查询关键词
     * @param field       查询维度：username（用户名）/ real_name（姓名）/ phone（手机号）
     * @return 用户列表；无权限、关键词为空或维度非法时返回空集合
     */
    public List<SysUser> findUsersByKeyword(SysUser currentUser, String keyword, String field) {
        if (!isAdmin(currentUser)) {
            System.out.println("关键词查询用户失败：没有管理员权限");
            return new ArrayList<>();
        }
        // 关键词去掉首尾空格后为空则不查数据库，直接返回空集合
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysUser> sysUserList = sysUserDao.findByNameLike(trimmed, field);
        System.out.println("关键词查询用户，共 " + sysUserList.size() + " 条");
        return sysUserList;
    }

    /**
     * 管理员启用/禁用用户（用户管理页状态按钮用）
     * - 管理员不能把自己禁用，防止把当前登录账号锁在系统外
     *
     * @param currentUser 当前登录用户
     * @param targetId    目标用户编号
     * @param status      新状态：STATUS_ENABLED（1）或 STATUS_DISABLED（0）
     * @return 修改成功返回 true；无权限、编号为空、把自己禁用或更新失败返回 false
     */
    public boolean updateStatus(SysUser currentUser, Integer targetId, int status) {
        if (!isAdmin(currentUser)) {
            System.out.println("修改用户状态失败：没有管理员权限");
            return false;
        }
        if (targetId == null) {
            System.out.println("修改用户状态失败：用户编号为空");
            return false;
        }
        // 禁用目标只能是其他用户：把自己禁用会导致当前会话无账号可用，提前拦截
        if (status == SysUser.STATUS_DISABLED && targetId.equals(currentUser.getId())) {
            System.out.println("修改用户状态失败：不能禁用当前登录账号");
            return false;
        }
        boolean result = sysUserDao.updateStatus(targetId, status);
        if (result) {
            System.out.println("修改用户状态成功：编号 " + targetId + "，新状态 " + status);
        } else {
            System.out.println("修改用户状态失败：用户不存在或数据库更新失败");
        }
        return result;
    }

    /**
     * 管理员新增用户
     * - 用户名由系统自动生成：管理员固定 00000，员工从 00001~99999 随机取未占用号码，调用方不能指定
     * - 角色、状态使用数据库默认值；初始密码由本方法生成并哈希后交给 DAO 保存
     *
     * @param currentUser 当前登录用户
     * @param sysUser     待新增的用户对象，只需提供真实姓名、手机号
     * @return 新增成功返回 true；无权限、字段为空、用户名生成失败或插入失败返回 false
     */
    public boolean addUser(SysUser currentUser, SysUser sysUser) {
        if (!isAdmin(currentUser)) {
            System.out.println("新增用户失败：没有管理员权限");
            return false;
        }
        if (sysUser == null) {
            System.out.println("新增用户失败：用户对象不能为空");
            return false;
        }
        // 检查非空字段是否为空（用户名由本方法生成，不在校验范围内）
        if (sysUser.getRealName() == null || sysUser.getRealName().isEmpty()) {
            System.out.println("新增用户失败：真实姓名为空");
            return false;
        }
        if (sysUser.getPhone() == null || sysUser.getPhone().isEmpty()) {
            System.out.println("新增用户失败：手机号为空");
            return false;
        }
        // 用户名按角色生成：管理员固定 00000；员工（含 role 为空走数据库默认值的情况）随机生成
        String username;
        if (sysUser.getRole() != null && sysUser.getRole() == SysUser.ROLE_ADMIN) {
            // 管理员用户名固定：已被占用说明系统已存在管理员账号，直接失败
            if (sysUserDao.findByUserName(ADMIN_USERNAME) != null) {
                System.out.println("新增用户失败：管理员账号已存在");
                return false;
            }
            username = ADMIN_USERNAME;
        } else {
            // 员工用户名随机生成，区间全部占用时给出明确失败，不无限循环
            username = generateStaffUsername();
            if (username == null) {
                System.out.println("新增用户失败：00001~99999 员工用户名已全部占用");
                return false;
            }
        }
        // 把生成的用户名写回实体，DAO 原样入库（username 列唯一约束兜底）
        sysUser.setUserName(username);
        // 初始密码在 Service 层完成 BCrypt 哈希，DAO 只负责原样入库
        sysUser.setPassword(hashPassword(DEFAULT_PASSWORD));
        boolean result = sysUserDao.add(sysUser);
        if (result) {
            System.out.println("新增用户成功：" + sysUser.getUserName());
        } else {
            System.out.println("新增用户失败：数据库写入失败");
        }
        return result;
    }

    /**
     * 随机生成一个未被占用的员工用户名（00001~99999，保留前导 0 的五位数字字符串）
     * - 生成后查库确认不存在，已占用则重新生成
     * - 用集合记录已尝试过的号码，每个号码最多查库一次，保证有限次内结束、不会死循环
     *
     * @return 可用的五位数字用户名；00001~99999 全部被占用时返回 null
     */
    private String generateStaffUsername() {
        // 已尝试过的号码集合：既避免同一号码反复查库，也作为生成结束的条件
        Set<Integer> tried = new HashSet<>();
        Random random = new Random();
        // 最多把区间内每个号码尝试一次，全部占用时结束循环
        while (tried.size() < 99999) {
            // 随机取 1~99999，格式化为五位数字（不足五位前补 0，如 1 → "00001"）
            int number = random.nextInt(99999) + 1;
            // add 返回 false 说明该号码已尝试过，重新抽取
            if (!tried.add(number)) {
                continue;
            }
            String username = String.format("%05d", number);
            // 号码未被占用即命中；已占用则继续尝试下一个号码
            if (sysUserDao.findByUserName(username) == null) {
                return username;
            }
        }
        // 走到这里说明 00001~99999 已全部占用，返回 null 由调用方给出明确提示
        return null;
    }

    /**
     * 管理员修改其他用户的基本信息
     * - 只修改真实姓名与手机号，用户名、密码、角色、状态均不变
     *
     * @param currentUser 当前登录用户
     * @param sysUser     携带新值并包含 id 的用户对象
     * @return 修改成功返回 true；无权限、编号为空、字段为空或更新失败返回 false
     */
    public boolean updateUser(SysUser currentUser, SysUser sysUser) {
        if (!isAdmin(currentUser)) {
            System.out.println("修改用户失败：没有管理员权限");
            return false;
        }
        if (sysUser == null || sysUser.getId() == null) {
            System.out.println("修改用户失败：用户对象为空或编号为空");
            return false;
        }
        if (sysUser.getRealName() == null || sysUser.getRealName().isEmpty()) {
            System.out.println("修改用户失败：真实姓名为空");
            return false;
        }
        if (sysUser.getPhone() == null || sysUser.getPhone().isEmpty()) {
            System.out.println("修改用户失败：手机号为空");
            return false;
        }
        boolean result = sysUserDao.update(sysUser);
        if (result) {
            System.out.println("修改用户成功：编号 " + sysUser.getId());
        } else {
            System.out.println("修改用户失败：用户不存在或数据库更新失败");
        }
        return result;
    }

    /**
     * 管理员重置其他用户的密码
     * - 不需要旧密码，由管理员身份担保；只能由管理员发起
     *
     * @param currentUser 当前登录用户
     * @param targetId    待重置密码的用户编号
     * @param newPassword 新密码
     * @return 重置成功返回 true；无权限、编号为空、用户不存在、新密码为空或更新失败返回 false
     */
    public boolean resetPassword(SysUser currentUser, Integer targetId, String newPassword) {
        if (!isAdmin(currentUser)) {
            System.out.println("重置密码失败：没有管理员权限");
            return false;
        }
        if (targetId == null) {
            System.out.println("重置密码失败：用户编号为空");
            return false;
        }
        if (newPassword == null || newPassword.isEmpty()) {
            System.out.println("重置密码失败：新密码为空");
            return false;
        }
        // 先确认目标用户存在，避免把密码更新到一个不存在的编号上却提示成功
        SysUser target = sysUserDao.findById(targetId);
        if (target == null) {
            System.out.println("重置密码失败：用户不存在");
            return false;
        }
        // 新密码同样先哈希再入库，不输出明文
        boolean result = sysUserDao.updatePassword(targetId, hashPassword(newPassword));
        if (result) {
            System.out.println("重置密码成功：用户 " + target.getUserName());
        } else {
            System.out.println("重置密码失败：数据库更新失败");
        }
        return result;
    }

    /**
     * 判断当前登录用户是否具备管理员权限
     *
     * @param currentUser 当前登录用户
     * @return 是管理员返回 true，用户为空或角色不是管理员返回 false
     */
    private boolean isAdmin(SysUser currentUser) {
        if (currentUser == null || currentUser.getRole() == null) {
            System.out.println("用户为空或角色为空");
            return false;
        }
        if (currentUser.getRole() == SysUser.ROLE_ADMIN) {
            System.out.println("用户是管理员");
            return true;
        }
        System.out.println("用户不是管理员");
            return false;   
    
    }

    /**
     * 把明文密码转换为 BCrypt 哈希串
     * - BCrypt 自动生成随机盐，同一明文每次哈希结果不同，属正常现象
     *
     * @param rawPassword 明文密码
     * @return 可入库保存的哈希串
     */
    private String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 验证明文密码与库中哈希是否匹配
     * - 待验证值为 null 时不可能匹配，先行拦截避免 BCrypt 抛空指针
     *
     * @param rawPassword    用户输入的明文密码
     * @param hashedPassword 数据库中保存的 BCrypt 哈希串
     * @return 匹配返回 true，任一为空或不匹配返回 false
     */
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

}
