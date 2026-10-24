import {Route, Switch} from 'wouter';
import {routes} from '@/routes';
import {HomePage, NotFoundPage} from "@/pages";


export const Router = () => {
    return (
        <Switch>
            <Route path={routes.HOME} component={HomePage}/>
            <Route component={NotFoundPage}/>
        </Switch>
    );
};
