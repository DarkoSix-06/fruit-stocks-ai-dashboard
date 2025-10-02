import axios from 'axios';

const API = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8081/api/v1',
});

export async function getSeries(startDate, endDate) {
  const { data } = await API.get('/stocks', { params: { startDate, endDate } });
  return data; // [{ date:"YYYY-MM-DD", fruit:"APPLE|ORANGE|BANANA", quantity:number }]
}

export async function getKpis(startDate, endDate) {
  const { data } = await API.get('/kpis', { params: { startDate, endDate } });
  return data; // { appleTotal, orangeTotal, bananaTotal, grandTotal, days }
}

export async function postSummarize(startDate, endDate) {
  const { data } = await API.post('/summarize', { startDate, endDate });
  return data; // { model, summary }
}
