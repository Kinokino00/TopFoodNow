import axios from 'axios';
import type { Categories } from '@/types/categories';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

if (!API_BASE_URL) {
    console.warn('VITE_API_BASE_URL is not defined. Please check your .env files.');
}

export async function getCategories(): Promise<Categories[]> {
    try {
        const response = await axios.get<Categories[]>(`${API_BASE_URL}/api/categories`);
        return response.data;
    } catch (error) {
        console.error('Error fetching categories:', error);
        throw error;
    }
}
