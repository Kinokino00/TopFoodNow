export function getToken(): string | null {
  return localStorage.getItem('authToken')
}

export function setToken(token: string): void {
  localStorage.setItem('authToken', token)
}

export function removeToken(): void {
  localStorage.removeItem('authToken')
}