import {AxiosError, type AxiosResponse} from 'axios';

export type ErrorResponseType = AxiosError & {
    response: AxiosResponse<{
        statusCode: number;
        message: string;
        error: string;
    }>;
};
