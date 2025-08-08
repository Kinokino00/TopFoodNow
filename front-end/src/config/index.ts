interface AppConfig {
    apiBaseUrl: string
}

const config: AppConfig = {
    apiBaseUrl: import.meta.env.VITE_API_BASE_URL,
}

export default config