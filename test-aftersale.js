import axios from 'axios'

const BASE_URL = 'http://localhost:8081/api'

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 15000
})

let userToken = ''
let adminToken = ''

const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m'
}

function log(title, message, color = colors.reset) {
  console.log(`${colors.bright}${colors.cyan}[${title}]${colors.reset} ${color}${message}${colors.reset}`)
}

function success(message) {
  log('SUCCESS', message, colors.green)
}

function error(message) {
  log('ERROR', message, colors.red)
}

function info(message) {
  log('INFO', message, colors.blue)
}

function warning(message) {
  log('WARNING', message, colors.yellow)
}

function printHeader(title) {
  console.log('\n' + '='.repeat(60))
  console.log(`${colors.bright}${colors.magenta}  ${title}${colors.reset}`)
  console.log('='.repeat(60))
}

async function loginUser() {
  try {
    const response = await api.post('/auth/login', {
      identifier: 'testuser',
      password: '123456'
    })
    if (response.data.code === 200) {
      userToken = response.data.data.token
      success('用户登录成功')
      console.log(`  Token: ${userToken.substring(0, 20)}...`)
      return true
    } else {
      warning('尝试注册新用户...')
      const codeResponse = await api.post('/auth/send-code', {
        identifier: 'testuser@test.com'
      })
      const code = codeResponse.data.data
      
      const registerResponse = await api.post('/auth/register', {
        username: 'testuser',
        identifier: 'testuser@test.com',
        code: code,
        password: '123456',
        nickname: '测试用户'
      })
      if (registerResponse.data.code === 200) {
        return await loginUser()
      }
    }
  } catch (e) {
    error('用户登录失败: ' + e.message)
    if (e.response) {
      console.log(`  响应: ${JSON.stringify(e.response.data)}`)
    }
    return false
  }
}

async function loginAdmin() {
  try {
    const response = await api.post('/auth/login', {
      identifier: 'admin',
      password: 'admin123'
    })
    if (response.data.code === 200) {
      adminToken = response.data.data.token
      success('管理员登录成功')
      console.log(`  Token: ${adminToken.substring(0, 20)}...`)
      return true
    }
  } catch (e) {
    warning('管理员登录失败，尝试使用普通用户token: ' + e.message)
    adminToken = userToken
    return false
  }
}

async function testCreateAfterSale() {
  printHeader('测试1: 创建售后申请')
  
  try {
    const afterSaleData = {
      orderId: 1,
      orderItemId: 1,
      type: 1,
      reason: '商品质量问题',
      description: '收到的商品有明显的质量缺陷，希望能够退款',
      amount: 99.00
    }
    
    const response = await api.post('/after-sale', afterSaleData, {
      headers: { 'Authorization': `Bearer ${userToken}` }
    })
    
    if (response.data.code === 200) {
      success('售后申请创建成功')
      return true
    } else {
      error('售后申请创建失败: ' + response.data.message)
      return false
    }
  } catch (e) {
    error('创建售后申请异常: ' + e.message)
    if (e.response) {
      console.log(`  状态码: ${e.response.status}`)
      console.log(`  响应: ${JSON.stringify(e.response.data)}`)
    }
    return false
  }
}

async function testGetUserAfterSales() {
  printHeader('测试2: 获取用户售后列表')
  
  try {
    const response = await api.get('/after-sale/list', {
      headers: { 'Authorization': `Bearer ${userToken}` }
    })
    
    if (response.data.code === 200) {
      const afterSales = response.data.data
      success('获取用户售后列表成功')
      console.log(`  售后数量: ${afterSales.length}`)
      afterSales.forEach((item, index) => {
        console.log(`  ${index + 1}. ID: ${item.id}, 状态: ${item.status}, 金额: ¥${item.amount}`)
      })
      return afterSales
    } else {
      error('获取售后列表失败: ' + response.data.message)
      return []
    }
  } catch (e) {
    error('获取售后列表异常: ' + e.message)
    return []
  }
}

async function testAdminGetAfterSales() {
  printHeader('测试3: 管理员获取售后列表')
  
  try {
    const response = await api.get('/admin/after-sales', {
      headers: { 'Authorization': `Bearer ${adminToken}` },
      params: { page: 1, size: 10 }
    })
    
    if (response.data.code === 200) {
      const pageData = response.data.data
      success('管理员获取售后列表成功')
      console.log(`  总数: ${pageData.total}`)
      console.log(`  当前页数量: ${pageData.records?.length || 0}`)
      pageData.records?.forEach((item, index) => {
        console.log(`  ${index + 1}. ID: ${item.id}, 用户ID: ${item.userId}, 状态: ${item.status}`)
      })
      return pageData.records || []
    } else {
      error('管理员获取售后列表失败: ' + response.data.message)
      return []
    }
  } catch (e) {
    error('管理员获取售后列表异常: ' + e.message)
    if (e.response) {
      console.log(`  状态码: ${e.response.status}`)
      console.log(`  响应: ${JSON.stringify(e.response.data)}`)
    }
    return []
  }
}

async function testAdminApproveAfterSale(afterSaleId) {
  printHeader(`测试4: 管理员同意售后申请 (ID: ${afterSaleId})`)
  
  try {
    const response = await api.put(`/admin/after-sales/${afterSaleId}/status`, {
      status: 2,
      remark: '同意申请，请等待退款'
    }, {
      headers: { 'Authorization': `Bearer ${adminToken}` }
    })
    
    if (response.data.code === 200) {
      success('售后申请同意成功')
      return true
    } else {
      error('售后申请同意失败: ' + response.data.message)
      return false
    }
  } catch (e) {
    error('同意售后申请异常: ' + e.message)
    return false
  }
}

async function testAdminRejectAfterSale(afterSaleId) {
  printHeader(`测试5: 管理员拒绝售后申请 (ID: ${afterSaleId})`)
  
  try {
    const response = await api.put(`/admin/after-sales/${afterSaleId}/status`, {
      status: 3,
      remark: '申请理由不充分，予以拒绝'
    }, {
      headers: { 'Authorization': `Bearer ${adminToken}` }
    })
    
    if (response.data.code === 200) {
      success('售后申请拒绝成功')
      return true
    } else {
      error('售后申请拒绝失败: ' + response.data.message)
      return false
    }
  } catch (e) {
    error('拒绝售后申请异常: ' + e.message)
    return false
  }
}

async function testAdminGetAfterSaleDetail(afterSaleId) {
  printHeader(`测试6: 管理员获取售后详情 (ID: ${afterSaleId})`)
  
  try {
    const response = await api.get(`/admin/after-sales/${afterSaleId}`, {
      headers: { 'Authorization': `Bearer ${adminToken}` }
    })
    
    if (response.data.code === 200) {
      const detail = response.data.data
      success('获取售后详情成功')
      console.log('  详情信息:')
      console.log(`    售后ID: ${detail.id}`)
      console.log(`    用户ID: ${detail.userId}`)
      console.log(`    订单ID: ${detail.orderId}`)
      console.log(`    类型: ${detail.type}`)
      console.log(`    原因: ${detail.reason}`)
      console.log(`    金额: ¥${detail.amount}`)
      console.log(`    状态: ${detail.status}`)
      console.log(`    管理员备注: ${detail.adminRemark || '无'}`)
      return detail
    } else {
      error('获取售后详情失败: ' + response.data.message)
      return null
    }
  } catch (e) {
    error('获取售后详情异常: ' + e.message)
    return null
  }
}

async function testAdminFilterAfterSales() {
  printHeader('测试7: 管理员按状态过滤售后列表')
  
  try {
    const response = await api.get('/admin/after-sales', {
      headers: { 'Authorization': `Bearer ${adminToken}` },
      params: { page: 1, size: 10, status: 1 }
    })
    
    if (response.data.code === 200) {
      const pageData = response.data.data
      success('按状态过滤售后列表成功')
      console.log(`  待处理数量: ${pageData.records?.length || 0}`)
      return true
    } else {
      error('过滤售后列表失败: ' + response.data.message)
      return false
    }
  } catch (e) {
    error('过滤售后列表异常: ' + e.message)
    return false
  }
}

async function main() {
  console.log('\n' + '='.repeat(60))
  console.log(`${colors.bright}${colors.magenta}  售后系统API自动测试${colors.reset}`)
  console.log('='.repeat(60))
  
  const results = {
    total: 0,
    passed: 0,
    failed: 0
  }
  
  info('开始测试...')
  info('后端地址: ' + BASE_URL)
  
  printHeader('第一步: 登录')
  
  const userLoginOk = await loginUser()
  if (!userLoginOk) {
    error('用户登录失败，终止测试')
    return
  }
  
  const adminLoginOk = await loginAdmin()
  if (!adminLoginOk) {
    warning('管理员登录失败，部分测试将跳过')
  }
  
  console.log()
  
  let afterSales = []
  let adminAfterSales = []
  
  if (userLoginOk) {
    results.total++
    const createOk = await testCreateAfterSale()
    createOk ? results.passed++ : results.failed++
    
    console.log()
    
    results.total++
    afterSales = await testGetUserAfterSales()
    afterSales.length > 0 ? results.passed++ : results.failed++
  }
  
  console.log()
  
  if (adminLoginOk) {
    results.total++
    adminAfterSales = await testAdminGetAfterSales()
    adminAfterSales.length > 0 ? results.passed++ : results.failed++
    
    console.log()
    
    if (adminAfterSales.length > 0) {
      const targetId = adminAfterSales[0].id
      
      results.total++
      const detailOk = await testAdminGetAfterSaleDetail(targetId)
      detailOk ? results.passed++ : results.failed++
      
      console.log()
      
      results.total++
      const approveOk = await testAdminApproveAfterSale(targetId)
      approveOk ? results.passed++ : results.failed++
      
      console.log()
      
      results.total++
      const filterOk = await testAdminFilterAfterSales()
      filterOk ? results.passed++ : results.failed++
    }
  }
  
  printHeader('测试结果汇总')
  console.log(`  总测试数: ${results.total}`)
  console.log(`  通过: ${colors.green}${results.passed}${colors.reset}`)
  console.log(`  失败: ${colors.red}${results.failed}${colors.reset}`)
  console.log(`  通过率: ${results.total > 0 ? ((results.passed / results.total) * 100).toFixed(1) : 0}%`)
  
  if (results.failed === 0 && results.total > 0) {
    console.log()
    success('所有测试通过！🎉')
  } else if (results.failed > 0) {
    console.log()
    warning('部分测试失败，请检查系统')
  }
  
  console.log()
}

main().catch(console.error)

