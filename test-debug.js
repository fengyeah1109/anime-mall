import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:5173/api',
  timeout: 15000
})

async function test() {
  const token = localStorage.getItem('token')
  console.log('Token found:', token ? 'Yes' : 'No')
  
  try {
    const response = await request.get('/after-sale/list', {
      headers: token ? { 'Authorization': `Bearer ${token}` } : {}
    })
    console.log('Response:', response)
  } catch (error) {
    console.error('Error:', error)
    console.error('Response:', error.response)
  }
}

test()
