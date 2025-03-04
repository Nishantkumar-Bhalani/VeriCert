const api = process.env.REACT_APP_CONTACTS_API_URL || 'http://localhost:3001'

const headers = {
  'Accept': 'application/json'
}

export const invokeTransaction = (payload) =>
  fetch(`${api}/invoke/`, {
    method: 'POST',
    headers: {
      ...headers,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  }).then(res => {
    return res.status
  }).catch(error => {
    console.log('This is error' + error)
    return error
  })
