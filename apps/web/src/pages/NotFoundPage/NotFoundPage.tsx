import {useLocation} from "wouter";
import {routes} from "@/routes";


export const NotFoundPage = () => {
    const [, navigate] = useLocation();

    return (
        <div>
            <p>Stranica koju tražiš ne postoji.</p>
            <button onClick={() => navigate(routes.HOME)}>
                Natrag na početnu
            </button>
        </div>
    );
};
