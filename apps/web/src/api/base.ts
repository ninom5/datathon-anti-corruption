import axios from 'axios';
import type {ErrorResponseType} from "@/types";

export const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.response.use(
    (response) => response.data,
    (error: ErrorResponseType) => {
        if (error.response) {
            return Promise.reject(
                new Error(error.response.data.message || error.message),
            );
        }

        return Promise.reject(new Error('Error while trying to intercept request'));
    },
);
