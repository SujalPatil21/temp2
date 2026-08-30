import axios from 'axios'
import type { Cluster } from '../types/cluster'
import type { Event } from '../types/event'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export const getAuthToken = () => localStorage.getItem('authToken') ?? sessionStorage.getItem('authToken')

export const clearAdminSession = () => {
  localStorage.removeItem('authToken')
  localStorage.removeItem('adminId')
  sessionStorage.removeItem('authToken')
  sessionStorage.removeItem('adminId')
}

export const saveAdminSession = (token: string, adminId: string | number, remember: boolean) => {
  const storage = remember ? localStorage : sessionStorage
  storage.setItem('authToken', token)
  storage.setItem('adminId', String(adminId))
}

api.interceptors.request.use((config) => {
  const token = getAuthToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) clearAdminSession()
    return Promise.reject(error)
  },
)

export interface RegisterAdminPayload {
  name: string
  email: string
  password: string
}

export interface LoginAdminPayload {
  email: string
  password: string
}

export interface OrganizerPayload {
  name: string
  phoneNumber: string
}

export interface CreateEventPayload {
  name: string
  centerLat: number
  centerLng: number
  radius: number
  startTime: string
  endTime: string
  organizers: OrganizerPayload[]
}

export const registerAdmin = async (payload: RegisterAdminPayload) => {
  const response = await api.post('/admin/register', payload)
  return response.data
}

export const loginAdmin = async (payload: LoginAdminPayload) => {
  const response = await api.post('/admin/login', payload)
  return response.data
}

export const createEvent = async (payload: CreateEventPayload) => {
  const response = await api.post('/events', payload)
  return response.data
}

export const getEventById = async (eventId: string) => {
  const response = await api.get<Event>(`/events/${eventId}`)
  return response.data
}

export const getClustersByEventId = async (eventId: string) => {
  const response = await api.get<Cluster[]>(`/admin/clusters/${eventId}`)
  return response.data
}

export const getActiveEvents = async () => {
  const response = await api.get<Event[]>('/events/admin/active')
  return response.data
}

export default api
