import axios from 'axios';
import type {ErrorResponseType} from "@/types";

/** Error with the backend's Croatian message and the HTTP status (undefined when the server was not reached). */
export class ApiError extends Error {
    readonly status: number | undefined;

    constructor(message: string, status: number | undefined) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

export const isClientError = (error: unknown) =>
    error instanceof ApiError && error.status !== undefined && error.status >= 400 && error.status < 500;

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
                new ApiError(error.response.data?.message || error.message, error.response.status),
            );
        }

        return Promise.reject(new ApiError('Poslužitelj nije dostupan. Provjerite vezu i pokušajte ponovno.', undefined));
    },
);
